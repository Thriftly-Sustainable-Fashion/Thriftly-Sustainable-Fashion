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
// Basic Health Check  
app.get('/', (req, res) => {  
    res.status(200).json({   
        status: 'healthy',  
        timestamp: new Date(),  
        service: 'thriftly-backend'  
    });  
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


// 5. Update Product  
app.put('/api/products/:id', authenticateUser, async (req, res) => {  
    try {  
        const { name, description, price, quantity, category_id, subcategory_id } = req.body;  
        const [result] = await pool.query(  
            'UPDATE products SET name = ?, description = ?, price = ?, quantity = ?, category_id = ?, subcategory_id = ? WHERE product_id = ?',  
            [name, description, price, quantity, category_id, subcategory_id, req.params.id]  
        );  
        if (result.affectedRows === 0) {  
            logger.warn('Product update failed: Product not found', { productId: req.params.id });  
            return res.status(404).json({ message: 'Product not found' });  
        }  
        logger.info('Product updated successfully:', { productId: req.params.id });  
        res.json({ message: 'Product updated successfully' });  
    } catch (error) {  
        logger.error('Error updating product:', error);  
        res.status(500).json({ message: 'Error updating product' });  
    }  
});  

// 6. Add to Cart  
app.post('/api/cart', authenticateUser, async (req, res) => {  
    try {  
        const { product_id, quantity } = req.body;  
        const user_id = req.user.user_id;  

        // Check if user has an active cart  
        let [cart] = await pool.query('SELECT * FROM carts WHERE user_id = ?', [user_id]);  
        let cart_id;  

        if (cart.length === 0) {  
            // Create new cart  
            const [newCart] = await pool.query('INSERT INTO carts (user_id) VALUES (?)', [user_id]);  
            cart_id = newCart.insertId;  
        } else {  
            cart_id = cart[0].cart_id;  
        }  

        // Add item to cart  
        await pool.query(  
            'INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)',  
            [cart_id, product_id, quantity]  
        );  

        logger.info('Item added to cart:', { userId: user_id, productId: product_id });  
        res.status(201).json({ message: 'Item added to cart successfully' });  
    } catch (error) {  
        logger.error('Error adding to cart:', error);  
        res.status(500).json({ message: 'Error adding to cart' });  
    }  
});  

// 7. Create Order  
app.post('/api/orders', authenticateUser, async (req, res) => {  
    try {  
        const { store_id, items } = req.body;  
        const user_id = req.user.user_id;  

        // Calculate total price  
        let total_price = 0;  
        for (const item of items) {  
            const [product] = await pool.query('SELECT price FROM products WHERE product_id = ?', [item.product_id]);  
            total_price += product[0].price * item.quantity;  
        }  

        // Create order  
        const [order] = await pool.query(  
            'INSERT INTO orders (user_id, store_id, total_price) VALUES (?, ?, ?)',  
            [user_id, store_id, total_price]  
        );  

        // Add order items  
        for (const item of items) {  
            await pool.query(  
                'INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)',  
                [order.insertId, item.product_id, item.quantity, item.price]  
            );  
        }  

        logger.info('Order created successfully:', { orderId: order.insertId });  
        res.status(201).json({  
            message: 'Order created successfully',  
            orderId: order.insertId  
        });  
    } catch (error) {  
        logger.error('Error creating order:', error);  
        res.status(500).json({ message: 'Error creating order' });  
    }  
});  

// 8. Get User Orders  
app.get('/api/orders', authenticateUser, async (req, res) => {  
    try {  
        const [orders] = await pool.query(  
            `SELECT o.*, oi.*   
             FROM orders o   
             LEFT JOIN order_items oi ON o.order_id = oi.order_id   
             WHERE o.user_id = ?`,  
            [req.user.user_id]  
        );  
        res.json(orders);  
    } catch (error) {  
        logger.error('Error fetching orders:', error);  
        res.status(500).json({ message: 'Error fetching orders' });  
    }  
});  

// 9. Create Store  
app.post('/api/stores', authenticateUser, async (req, res) => {  
    try {  
        const { name, description, address } = req.body;  
        const owner_id = req.user.user_id;  

        const [result] = await pool.query(  
            'INSERT INTO stores (owner_id, name, description, address) VALUES (?, ?, ?, ?)',  
            [owner_id, name, description, address]  
        );  

        logger.info('Store created successfully:', { storeId: result.insertId });  
        res.status(201).json({  
            message: 'Store created successfully',  
            storeId: result.insertId  
        });  
    } catch (error) {  
        logger.error('Error creating store:', error);  
        res.status(500).json({ message: 'Error creating store' });  
    }  
});  

// 10. Update Store  
app.put('/api/stores/:id', authenticateUser, async (req, res) => {  
    try {  
        const { name, description, address } = req.body;  
        const [result] = await pool.query(  
            'UPDATE stores SET name = ?, description = ?, address = ? WHERE store_id = ? AND owner_id = ?',  
            [name, description, address, req.params.id, req.user.user_id]  
        );  

        if (result.affectedRows === 0) {  
            return res.status(404).json({ message: 'Store not found or unauthorized' });  
        }  

        logger.info('Store updated successfully:', { storeId: req.params.id });  
        res.json({ message: 'Store updated successfully' });  
    } catch (error) {  
        logger.error('Error updating store:', error);  
        res.status(500).json({ message: 'Error updating store' });  
    }  
});  

// 11. Create Review  
app.post('/api/reviews', authenticateUser, async (req, res) => {  
    try {  
        const { store_id, rating, comment } = req.body;  
        const user_id = req.user.user_id;  

        // Validate rating  
        if (rating < 1 || rating > 5) {  
            return res.status(400).json({ message: 'Rating must be between 1 and 5' });  
        }  

        const [result] = await pool.query(  
            'INSERT INTO reviews (store_id, user_id, rating, comment) VALUES (?, ?, ?, ?)',  
            [store_id, user_id, rating, comment]  
        );  

        logger.info('Review created successfully:', { reviewId: result.insertId });  
        res.status(201).json({  
            message: 'Review created successfully',  
            reviewId: result.insertId  
        });  
    } catch (error) {  
        logger.error('Error creating review:', error);  
        res.status(500).json({ message: 'Error creating review' });  
    }  
});  

// 12. Get Store Reviews  
app.get('/api/stores/:storeId/reviews', async (req, res) => {  
    try {  
        const [reviews] = await pool.query(  
            `SELECT r.*, u.name as user_name   
             FROM reviews r   
             JOIN users u ON r.user_id = u.user_id   
             WHERE r.store_id = ?  
             ORDER BY r.created_at DESC`,  
            [req.params.storeId]  
        );  
        res.json(reviews);  
    } catch (error) {  
        logger.error('Error fetching reviews:', error);  
        res.status(500).json({ message: 'Error fetching reviews' });  
    }  
});  

// 13. Get User Notifications  
app.get('/api/notifications', authenticateUser, async (req, res) => {  
    try {  
        const [notifications] = await pool.query(  
            'SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC',  
            [req.user.user_id]  
        );  

        // Mark notifications as read  
        await pool.query(  
            'UPDATE notifications SET status = "read" WHERE user_id = ? AND status = "unread"',  
            [req.user.user_id]  
        );  

        res.json(notifications);  
    } catch (error) {  
        logger.error('Error fetching notifications:', error);  
        res.status(500).json({ message: 'Error fetching notifications' });  
    }  
});  

// 14. Search Products and Stores  
app.get('/api/search', async (req, res) => {  
    try {  
        const { query, type = 'all', category_id, min_price, max_price } = req.query;  
        let sql = '';  
        let params = [];  

        if (type === 'products' || type === 'all') {  
            sql += `  
                SELECT 'product' as type, p.*, c.name as category_name   
                FROM products p  
                JOIN category c ON p.category_id = c.category_id  
                WHERE p.name LIKE ? OR p.description LIKE ?  
            `;  
            if (category_id) {  
                sql += ' AND p.category_id = ?';  
                params.push(category_id);  
            }  
            if (min_price) {  
                sql += ' AND p.price >= ?';  
                params.push(min_price);  
            }  
            if (max_price) {  
                sql += ' AND p.price <= ?';  
                params.push(max_price);  
            }  
        }  

        if (type === 'stores' || type === 'all') {  
            if (sql) sql += ' UNION ';  
            sql += `  
                SELECT 'store' as type, s.*   
                FROM stores s   
                WHERE s.name LIKE ? OR s.description LIKE ?  
            `;  
        }  

        const searchQuery = `%${query}%`;  
        params = [searchQuery, searchQuery, ...params];  

        const [results] = await pool.query(sql, params);  

        // Log search query  
        if (req.user) {  
            await pool.query(  
                'INSERT INTO search_history (user_id, search_query) VALUES (?, ?)',  
                [req.user.user_id, query]  
            );  
        }  

        res.json(results);  
    } catch (error) {  
        logger.error('Error performing search:', error);  
        res.status(500).json({ message: 'Error performing search' });  
    }  
});  

// 15. Add to Wishlist  
app.post('/api/wishlist', authenticateUser, async (req, res) => {  
    try {  
        const { product_id } = req.body;  
        const user_id = req.user.user_id;  

        await pool.query(  
            'INSERT INTO wishlist (user_id, product_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP',  
            [user_id, product_id]  
        );  

        logger.info('Item added to wishlist:', { userId: user_id, productId: product_id });  
        res.status(201).json({ message: 'Item added to wishlist successfully' });  
    } catch (error) {  
        logger.error('Error adding to wishlist:', error);  
        res.status(500).json({ message: 'Error adding to wishlist' });  
    }  
});  

// 16. Get User's Wishlist  
app.get('/api/wishlist', authenticateUser, async (req, res) => {  
    try {  
        const [wishlist] = await pool.query(  
            `SELECT w.*, p.*, s.name as store_name   
             FROM wishlist w   
             JOIN products p ON w.product_id = p.product_id   
             JOIN stores s ON p.store_id = s.store_id   
             WHERE w.user_id = ?`,  
            [req.user.user_id]  
        );  
        res.json(wishlist);  
    } catch (error) {  
        logger.error('Error fetching wishlist:', error);  
        res.status(500).json({ message: 'Error fetching wishlist' });  
    }  
});  

// 17. Get Categories with Subcategories  
app.get('/api/categories', async (req, res) => {  
    try {  
        const [categories] = await pool.query(  
            `SELECT c.*,   
             (SELECT JSON_ARRAYAGG(  
                JSON_OBJECT('id', s.subcategory_id, 'name', s.name, 'description', s.description)  
             )   
             FROM subcategories s   
             WHERE s.category_id = c.category_id) as subcategories   
             FROM category c`  
        );  
        res.json(categories);  
    } catch (error) {  
        logger.error('Error fetching categories:', error);  
        res.status(500).json({ message: 'Error fetching categories' });  
    }  
});  

// 18. Get Store Analytics  
app.get('/api/stores/:storeId/analytics', authenticateUser, async (req, res) => {  
    try {  
        const store_id = req.params.storeId;  

        // Verify store ownership  
        const [store] = await pool.query(  
            'SELECT * FROM stores WHERE store_id = ? AND owner_id = ?',  
            [store_id, req.user.user_id]  
        );  

        if (store.length === 0) {  
            return res.status(403).json({ message: 'Unauthorized access to store analytics' });  
        }  

        // Get various analytics  
        const [orderStats] = await pool.query(  
            `SELECT   
                COUNT(*) as total_orders,  
                SUM(total_price) as total_revenue,  
                AVG(total_price) as average_order_value  
             FROM orders   
             WHERE store_id = ?`,  
            [store_id]  
        );  

        const [productStats] = await pool.query(  
            `SELECT   
                COUNT(*) as total_products,  
                AVG(price) as average_price,  
                SUM(quantity) as total_inventory  
             FROM products   
             WHERE store_id = ?`,  
            [store_id]  
        );  

        const [reviewStats] = await pool.query(  
            `SELECT   
                COUNT(*) as total_reviews,  
                AVG(rating) as average_rating  
             FROM reviews   
             WHERE store_id = ?`,  
            [store_id]  
        );  

        res.json({  
            orderStats: orderStats[0],  
            productStats: productStats[0],  
            reviewStats: reviewStats[0]  
        });  
    } catch (error) {  
        logger.error('Error fetching store analytics:', error);  
        res.status(500).json({ message: 'Error fetching store analytics' });  
    }  
});  


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
