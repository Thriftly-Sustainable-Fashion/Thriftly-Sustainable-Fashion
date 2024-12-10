const express = require('express');  
const tf = require('@tensorflow/tfjs-node');  
const path = require('path');  
const cors = require('cors');  
const winston = require('winston');  

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

// Global variable to store the loaded model  
let model;  

// Function to load the model  
const loadModel = async () => {  
    try {  
        // Update the model path to the correct location  
        const modelPath = 'file://' + path.join(__dirname, 'models/tfjs_collaborative_filtering_model/model.json');  
        logger.info(`Attempting to load model from: ${modelPath}`);  

        // Load the model from the specified path  
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

// Endpoint for getting recommendations  
app.post('/recommendations', async (req, res) => {  
    try {  
        const { userId, numRecommendations = 5 } = req.body;  

        if (!model) {  
            return res.status(503).json({  
                message: 'Model not loaded yet'  
            });  
        }  

        // Convert userId to tensor  
        const inputTensor = tf.tensor2d([[userId]]);  

        // Get predictions from the model  
        const predictions = model.predict(inputTensor);  
        const predictionArray = await predictions.array();  

        // Clean up tensors  
        inputTensor.dispose();  
        predictions.dispose();  

        // Process predictions and return top N recommendations  
        const recommendations = predictionArray[0]  
            .map((score, index) => ({ productId: index, score }))  
            .sort((a, b) => b.score - a.score)  
            .slice(0, numRecommendations);  

        logger.info(`Generated recommendations for user ${userId}`);  
        res.json({ recommendations });  

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
