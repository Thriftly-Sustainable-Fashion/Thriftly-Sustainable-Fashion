const tf = require('@tensorflow/tfjs-node');  
const express = require('express');  
const cors = require('cors');  
const mysql = require('mysql2/promise');  
require('dotenv').config();  
  
const app = express();  
app.use(cors());  
app.use(express.json());  
  
let model;  
  
// Database connection  
async function connectToDatabase() {  
    const connection = await mysql.createConnection({  
        host: process.env.DB_HOST,  
        user: process.env.DB_USER,  
        password: process.env.DB_PASSWORD,  
        database: process.env.DB_NAME  
    });  
    return connection;  
}  
  
// Load model function  
async function loadModel() {  
    try {  
        model = await tf.loadLayersModel('file://tfjs_collaborative_filtering_model/model.json');  
        console.log('Model loaded successfully');  
        return model;  
    } catch (error) {  
        console.error('Error loading model:', error);  
        throw error;  
    }  
}  
  
// Initialize model  
loadModel();  
  
// Prediction endpoint  
app.post('/api/predict', async (req, res) => {  
    try {  
        const { userId, productId } = req.body;  
  
        // Validate input ranges  
        if (userId < 0 || userId >= 50705 || productId < 0 || productId >= 44446) {  
            return res.status(400).json({  
                success: false,  
                error: 'Invalid user ID or product ID range'  
            });  
        }  
  
        // Convert inputs to tensor  
        const userTensor = tf.tensor2d([[userId]], [1, 1]);  
        const productTensor = tf.tensor2d([[productId]], [1, 1]);  
  
        // Get prediction  
        const prediction = model.predict([userTensor, productTensor]);  
        const score = await prediction.data();  
  
        // Save to database  
        const connection = await connectToDatabase();  
        await connection.execute(  
            'INSERT INTO recommendations (user_id, product_id, score, created_at) VALUES (?, ?, ?, NOW())',  
            [userId, productId, score[0]]  
        );  
        await connection.end();  
  
        res.json({  
            success: true,  
            prediction: score[0],  
            userId,  
            productId  
        });  
  
    } catch (error) {  
        console.error('Prediction error:', error);  
        res.status(500).json({  
            success: false,  
            error: error.message  
        });  
    }  
});  
  
// Get recommendations for user  
app.get('/api/recommendations/:userId', async (req, res) => {  
    try {  
        const userId = parseInt(req.params.userId);  
          
        // Validate user ID  
        if (userId < 0 || userId >= 50705) {  
            return res.status(400).json({  
                success: false,  
                error: 'Invalid user ID range'  
            });  
        }  
  
        const connection = await connectToDatabase();  
          
        // Get top 10 recommendations for user  
        const [recommendations] = await connection.execute(  
            `SELECT r.*, p.product_name, p.product_image   
             FROM recommendations r  
             LEFT JOIN products p ON r.product_id = p.id  
             WHERE r.user_id = ?  
             ORDER BY r.score DESC  
             LIMIT 10`,  
            [userId]  
        );  
          
        await connection.end();  
  
        res.json({  
            success: true,  
            recommendations  
        });  
  
    } catch (error) {  
        console.error('Get recommendations error:', error);  
        res.status(500).json({  
            success: false,  
            error: error.message  
        });  
    }  
});  
  
// Batch prediction endpoint  
app.post('/api/predict-batch', async (req, res) => {  
    try {  
        const { userIds, productIds } = req.body;  
  
        if (!Array.isArray(userIds) || !Array.isArray(productIds) ||   
            userIds.length !== productIds.length) {  
            return res.status(400).json({  
                success: false,  
                error: 'Invalid input arrays'  
            });  
        }  
  
        // Validate ranges  
        if (userIds.some(id => id < 0 || id >= 50705) ||   
            productIds.some(id => id < 0 || id >= 44446)) {  
            return res.status(400).json({  
                success: false,  
                error: 'Invalid ID ranges in batch'  
            });  
        }  
  
        const userTensor = tf.tensor2d(userIds, [userIds.length, 1]);  
        const productTensor = tf.tensor2d(productIds, [productIds.length, 1]);  
  
        const predictions = model.predict([userTensor, productTensor]);  
        const scores = await predictions.data();  
  
        // Save batch predictions to database  
        const connection = await connectToDatabase();  
        const values = userIds.map((userId, index) => [  
            userId,  
            productIds[index],  
            scores[index]  
        ]);  
  
        await connection.query(  
            'INSERT INTO recommendations (user_id, product_id, score) VALUES ?',  
            [values]  
        );  
        await connection.end();  
  
        res.json({  
            success: true,  
            predictions: userIds.map((userId, index) => ({  
                userId,  
                productId: productIds[index],  
                score: scores[index]  
            }))  
        });  
  
    } catch (error) {  
        console.error('Batch prediction error:', error);  
        res.status(500).json({  
            success: false,  
            error: error.message  
        });  
    }  
});  
  
// Health check endpoint  
app.get('/api/health', (req, res) => {  
    res.json({   
        status: 'healthy',  
        modelLoaded: model !== undefined  
    });  
});  
  
const PORT = process.env.PORT || 8080;  
app.listen(PORT, () => {  
    console.log(`Server running on port ${PORT}`);  
});  
