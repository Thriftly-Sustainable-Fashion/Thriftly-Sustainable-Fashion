const express = require('express');  
const tf = require('@tensorflow/tfjs-node');  
const mysql = require('mysql2/promise');  
const path = require('path');  
const cors = require('cors');  
const winston = require('winston');  
require('dotenv').config();  

// Initialize Express app  
const app = express();  
const PORT = process.env.PORT || 8080;  

// Middleware  
app.use(cors());  
app.use(express.json());  

// Winston Logger Configuration  
const logger = winston.createLogger({  
    level: 'info',  
    format: winston.format.combine(  
        winston.format.timestamp(),  
        winston.format.json()  
    ),  
    transports: [  
        new winston.transports.Console(),  
        new winston.transports.File({ filename: 'ml-model.log' })  
    ]  
});  

// Database Configuration  
const dbConfig = {  
    host: process.env.DB_HOST || '34.101.195.146',  
    user: process.env.DB_USER || 'root',  
    password: process.env.DB_PASSWORD || 'kimochi:)-!@#',  
    database: process.env.DB_NAME || 'thriftly-mysql-db',  
    waitForConnections: true,  
    connectionLimit: 10,  
    queueLimit: 0  
};  

// Create Database Pool  
const pool = mysql.createPool(dbConfig);  

// Test Database Connection  
pool.getConnection()  
    .then(connection => {  
        logger.info('Database connected successfully');  
        connection.release();  
    })  
    .catch(error => {  
        logger.error('Database connection failed:', error);  
        process.exit(1);  
    });  

// Global variable to store the loaded model  
let model;  

// Function to load the model  
const loadModel = async () => {  
    try {  
        const modelPath = path.join(__dirname, 'tfjs_collaborative_filtering_model', 'model.json');  
        logger.info(`Attempting to load model from: ${modelPath}`);  
        model = await tf.loadLayersModel(modelPath);  
        logger.info('Model loaded successfully');  
        return true;  
    } catch (error) {  
        logger.error('Error loading model:', error);  
        return false;  
    }  
};  

// Health check endpoint  
app.get('/', (req, res) => {  
    res.status(200).json({  
        status: 'healthy',  
        timestamp: new Date(),  
        service: 'thriftly-ml-model'  
    });  
});  

// Get product recommendations  
app.post('/api/recommendations', async (req, res) => {  
    try {  
        const { userId, numRecommendations = 5 } = req.body;  

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

        // Convert userId to tensor  
        const inputTensor = tf.tensor2d([[userId]]);  

        // Get predictions from the model  
        const predictions = model.predict(inputTensor);  
        const predictionArray = await predictions.array();  

        // Clean up tensors  
        inputTensor.dispose();  
        predictions.dispose();  

        // Process predictions and filter out already purchased products  
        const purchasedProductIds = new Set(userHistory.map(h => h.product_id));  
        const recommendations = predictionArray[0]  
            .map((score, index) => ({ productId: index, score }))  
            .filter(rec => !purchasedProductIds.has(rec.productId))  
            .sort((a, b) => b.score - a.score)  
            .slice(0, numRecommendations);  

        // Get product details for recommendations  
        const productIds = recommendations.map(r => r.productId);  
        if (productIds.length > 0) {  
            const [products] = await pool.query(  
                'SELECT * FROM products WHERE product_id IN (?)',  
                [productIds]  
            );  

            // Combine predictions with product details  
            const fullRecommendations = recommendations.map(rec => ({  
                ...rec,  
                product: products.find(p => p.product_id === rec.productId)  
            }));  

            logger.info(`Generated recommendations for user ${userId}`);  
            res.json({ recommendations: fullRecommendations });  
        } else {  
            res.json({ recommendations: [] });  
        }  

    } catch (error) {  
        logger.error('Error generating recommendations:', error);  
        res.status(500).json({  
            message: 'Error generating recommendations',  
            error: error.message  
        });  
    }  
});  

// Error handling middleware  
app.use((err, req, res, next) => {  
    logger.error('Unhandled error:', err);  
    res.status(500).json({ message: 'Internal server error' });  
});  

// Start the server and load the model  
const startServer = async () => {  
    try {  
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
