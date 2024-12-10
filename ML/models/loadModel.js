const express = require('express');  
const tf = require('@tensorflow/tfjs-node');  
const mysql = require('mysql2/promise');  
const path = require('path');  
const cors = require('cors');  
const winston = require('winston');  
require('dotenv').config();  

// Winston Logger Configuration  
const logger = winston.createLogger({  
    level: process.env.LOG_LEVEL || 'info',  
    format: winston.format.combine(  
        winston.format.timestamp(),  
        winston.format.errors({ stack: true }),  
        winston.format.json()  
    ),  
    transports: [  
        new winston.transports.Console({  
            format: winston.format.combine(  
                winston.format.colorize(),  
                winston.format.simple()  
            )  
        }),  
        new winston.transports.File({   
            filename: 'error.log',   
            level: 'error',  
            maxsize: 5242880, // 5MB  
            maxFiles: 5,  
        }),  
        new winston.transports.File({   
            filename: 'combined.log',  
            maxsize: 5242880, // 5MB  
            maxFiles: 5,  
        })  
    ]  
});  

// Log environment information  
logger.info('Environment Configuration:', {  
    NODE_ENV: process.env.NODE_ENV,  
    PORT: process.env.PORT,  
    DB_HOST: process.env.DB_HOST,  
    DB_NAME: process.env.DB_NAME,  
    DB_USER: process.env.DB_USER  
});  

// Initialize Express app  
const app = express();  
const PORT = process.env.PORT || 8080;  

// Middleware  
app.use(cors());  
app.use(express.json());  
app.use(express.urlencoded({ extended: true }));  

// Request logging middleware  
app.use((req, res, next) => {  
    logger.info(`${req.method} ${req.url}`, {  
        ip: req.ip,  
        userAgent: req.get('user-agent')  
    });  
    next();  
});  

// Database Configuration  
const dbConfig = {  
    host: process.env.DB_HOST,  
    user: process.env.DB_USER,  
    password: process.env.DB_PASSWORD,  
    database: process.env.DB_NAME,  
    waitForConnections: true,  
    connectionLimit: 10,  
    queueLimit: 0,  
    enableKeepAlive: true,  
    keepAliveInitialDelay: 0  
};  

// Create Database Pool  
const pool = mysql.createPool(dbConfig);  

// Test Database Connection  
const testDatabaseConnection = async () => {  
    try {  
        const connection = await pool.getConnection();  
        logger.info('Database connected successfully');  
        connection.release();  
        return true;  
    } catch (error) {  
        logger.error('Database connection failed:', error);  
        return false;  
    }  
};  

// Global variable to store the loaded model  
let model;  

// Function to load the model  
const loadModel = async () => {  
    try {  
        const modelPath = `file://${path.join(__dirname, 'tfjs_collaborative_filtering_model', 'model.json')}`;  
        logger.info(`Loading model from: ${modelPath}`);  

        model = await tf.loadLayersModel(modelPath);  
        logger.info('Model loaded successfully');  

        // Warm up the model with a sample prediction  
        const warmupTensor = tf.tensor2d([[1]]);  
        const warmupPrediction = model.predict(warmupTensor);  
        warmupPrediction.dispose();  
        warmupTensor.dispose();  

        return true;  
    } catch (error) {  
        logger.error('Error loading model:', error);  
        return false;  
    }  
};  

// Health check endpoint  
app.get('/health', (req, res) => {  
    res.status(200).json({  
        status: 'healthy',  
        timestamp: new Date().toISOString(),  
        service: 'thriftly-ml-model',  
        modelLoaded: !!model,  
        uptime: process.uptime()  
    });  
});  

// Get product recommendations  
app.post('/api/recommendations', async (req, res) => {  
    const startTime = Date.now();  
    try {  
        const { userId, numRecommendations = 5 } = req.body;  

        if (!userId) {  
            return res.status(400).json({  
                message: 'userId is required'  
            });  
        }  

        if (!model) {  
            return res.status(503).json({  
                message: 'Model not loaded yet'  
            });  
        }  

        // Get user's purchase history  
        const [userHistory] = await pool.query(  
            `SELECT DISTINCT product_id   
             FROM order_items oi   
             JOIN orders o ON oi.order_id = o.order_id   
             WHERE o.user_id = ?`,  
            [userId]  
        );  

        // Generate predictions  
        const inputTensor = tf.tensor2d([[userId]]);  
        const predictions = model.predict(inputTensor);  
        const predictionArray = await predictions.array();  

        // Clean up tensors  
        inputTensor.dispose();  
        predictions.dispose();  

        // Process predictions  
        const purchasedProductIds = new Set(userHistory.map(h => h.product_id));  
        const recommendations = predictionArray[0]  
            .map((score, index) => ({ productId: index, score }))  
            .filter(rec => !purchasedProductIds.has(rec.productId))  
            .sort((a, b) => b.score - a.score)  
            .slice(0, numRecommendations);  

        // Get product details  
        if (recommendations.length > 0) {  
            const productIds = recommendations.map(r => r.productId);  
            const [products] = await pool.query(  
                'SELECT * FROM products WHERE product_id IN (?)',  
                [productIds]  
            );  

            const fullRecommendations = recommendations.map(rec => ({  
                ...rec,  
                product: products.find(p => p.product_id === rec.productId) || null  
            }));  

            logger.info(`Generated recommendations for user ${userId}`, {  
                duration: Date.now() - startTime,  
                recommendationCount: fullRecommendations.length  
            });  

            res.json({   
                recommendations: fullRecommendations,  
                processingTime: Date.now() - startTime  
            });  
        } else {  
            res.json({   
                recommendations: [],  
                processingTime: Date.now() - startTime  
            });  
        }  

    } catch (error) {  
        logger.error('Error generating recommendations:', error);  
        res.status(500).json({  
            message: 'Error generating recommendations',  
            error: process.env.NODE_ENV === 'development' ? error.message : 'Internal server error'  
        });  
    }  
});  

// Error handling middleware  
app.use((err, req, res, next) => {  
    logger.error('Unhandled error:', err);  
    res.status(500).json({   
        message: 'Internal server error',  
        error: process.env.NODE_ENV === 'development' ? err.message : undefined  
    });  
});  

// Handle uncaught exceptions  
process.on('uncaughtException', (error) => {  
    logger.error('Uncaught Exception:', error);  
    process.exit(1);  
});  

// Handle unhandled rejections  
process.on('unhandledRejection', (reason, promise) => {  
    logger.error('Unhandled Rejection:', reason);  
    process.exit(1);  
});  

// Start the server  
const startServer = async () => {  
    try {  
        // Test database connection  
        const dbConnected = await testDatabaseConnection();  
        if (!dbConnected) {  
            throw new Error('Database connection failed');  
        }  

        // Load the model  
        const modelLoaded = await loadModel();  
        if (!modelLoaded) {  
            throw new Error('Failed to load model');  
        }  

        // Start the server  
        app.listen(PORT, '0.0.0.0', () => {  
            logger.info(`ML Model server running on port ${PORT}`);  
        });  
    } catch (error) {  
        logger.error('Failed to start server:', error);  
        process.exit(1);  
    }  
};  

// Start the server  
startServer();  

module.exports = app;  
