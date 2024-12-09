// Import required modules  
const express = require('express');  
const mysql = require('mysql2/promise');  
const bcrypt = require('bcryptjs');  
const bodyParser = require('body-parser');  
const cors = require('cors');  
const winston = require('winston');  
require('dotenv').config();  

// Initialize Express app  
const app = express();  
const PORT = process.env.PORT || 8080;  

// Middleware  
app.use(cors());  
app.use(bodyParser.json());  

// Winston Logger Configuration  
const logger = winston.createLogger({  
    level: 'info',  
    format: winston.format.combine(  
        winston.format.timestamp(),  
        winston.format.json()  
    ),  
    transports: [  
        new winston.transports.Console(),  
        new winston.transports.File({ filename: 'app.log' })  
    ]  
});  

// Database Connection Pool  
const pool = mysql.createPool({  
    host: process.env.DB_HOST || '34.101.195.146',  
    user: process.env.DB_USER || 'root',  
    password: process.env.DB_PASSWORD || 'kimochi:)-!@#',  
    database: process.env.DB_NAME || 'thriftly-mysql-db',  
    waitForConnections: true,  
    connectionLimit: 10,  
    queueLimit: 0  
});  

// Authentication Middleware  
const authenticateUser = async (req, res, next) => {  
    const { email, password } = req.body;  
    try {  
        const [users] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);  
        if (users.length === 0) {  
            logger.warn('Invalid login attempt: User not found');  
            return res.status(401).json({ message: 'Invalid credentials' });  
        }  
        const user = users[0];  
        const isValid = await bcrypt.compare(password, user.password_hash);  
        if (!isValid) {  
            logger.warn('Invalid login attempt: Incorrect password');  
            return res.status(401).json({ message: 'Invalid credentials' });  
        }  
        req.user = user;  
        next();  
    } catch (error) {  
        logger.error('Authentication error:', error);  
        res.status(500).json({ message: 'Authentication error' });  
    }  
};  

// Routes  

// 1. Register User  
app.post('/api/users/register', async (req, res) => {  
    try {  
        const { name, email, password, phone_number } = req.body;  
        const [existing] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);  
        if (existing.length > 0) {  
            logger.warn('Registration attempt with existing email:', email);  
            return res.status(400).json({ message: 'Email already registered' });  
        }  
        const password_hash = await bcrypt.hash(password, 10);  
        const [result] = await pool.query(  
            'INSERT INTO users (name, email, password_hash, phone_number) VALUES (?, ?, ?, ?)',  
            [name, email, password_hash, phone_number]  
        );  
        logger.info('User registered successfully:', { userId: result.insertId });  
        res.status(201).json({  
            message: 'User registered successfully',  
            userId: result.insertId  
        });  
    } catch (error) {  
        logger.error('Registration error:', error);  
        res.status(500).json({ message: 'Registration error' });  
    }  
});  

// 2. Login User  
app.post('/api/users/login', authenticateUser, (req, res) => {  
    const { password_hash, ...user } = req.user;  
    logger.info('User logged in successfully:', { userId: user.user_id });  
    res.json({  
        message: 'Login successful',  
        user  
    });  
});  

// 3. Get Product by ID  
app.get('/api/products/:id', async (req, res) => {  
    try {  
        const [product] = await pool.query('SELECT * FROM products WHERE product_id = ?', [req.params.id]);  
        if (product.length === 0) {  
            logger.warn('Product not found:', { productId: req.params.id });  
            return res.status(404).json({ message: 'Product not found' });  
        }  
        res.json(product[0]);  
    } catch (error) {  
        logger.error('Error fetching product:', error);  
        res.status(500).json({ message: 'Error fetching product' });  
    }  
});  

// 4. Create Product  
app.post('/api/products', authenticateUser, async (req, res) => {  
    try {  
        const { store_id, name, description, price, quantity, category_id, subcategory_id } = req.body;  
        const [result] = await pool.query(  
            'INSERT INTO products (store_id, name, description, price, quantity, category_id, subcategory_id) VALUES (?, ?, ?, ?, ?, ?, ?)',  
            [store_id, name, description, price, quantity, category_id, subcategory_id]  
        );  
        logger.info('Product created successfully:', { productId: result.insertId });  
        res.status(201).json({  
            message: 'Product created successfully',  
            productId: result.insertId  
        });  
    } catch (error) {  
        logger.error('Error creating product:', error);  
        res.status(500).json({ message: 'Error creating product' });  
    }  
});  

// Add other routes here (e.g., update product, delete product, cart operations, etc.)  

// Error Handling Middleware  
app.use((err, req, res, next) => {  
    logger.error('Unhandled error:', err);  
    res.status(500).json({ message: 'Internal server error' });  
});  

// Start Server  
app.listen(PORT, '0.0.0.0', () => {  
    logger.info(`Server running on port ${PORT}`);  
});  

module.exports = app;  
