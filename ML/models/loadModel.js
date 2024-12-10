const tf = require('@tensorflow/tfjs-node');  
const express = require('express');  
const cors = require('cors');  
const mysql = require('mysql2/promise'); // Import mysql2  
require('dotenv').config();  

const app = express();  
app.use(cors());  
app.use(express.json());  

let model;  

// Koneksi ke database  
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
app.post('/predict', async (req, res) => {  
    try {  
        const { userId, productId } = req.body;  

        // Convert inputs to tensor  
        const userTensor = tf.tensor2d([[userId, productId]]);  

        // Get prediction  
        const prediction = model.predict(userTensor);  
        const score = await prediction.data();  

        // Simpan hasil ke database  
        const connection = await connectToDatabase();  
        await connection.execute('INSERT INTO predictions (userId, productId, score) VALUES (?, ?, ?)', [userId, productId, score[0]]);  
        await connection.end();  

        res.json({  
            success: true,  
            prediction: score[0],  
            userId,  
            productId  
        });  
    } catch (error) {  
        res.status(500).json({  
            success: false,  
            error: error.message  
        });  
    }  
});  

// Health check endpoint  
app.get('/health', (req, res) => {  
    res.json({ status: 'healthy' });  
});  

const PORT = process.env.PORT || 8080;  
app.listen(PORT, () => {  
    console.log(`Server running on port ${PORT}`);  
});  
