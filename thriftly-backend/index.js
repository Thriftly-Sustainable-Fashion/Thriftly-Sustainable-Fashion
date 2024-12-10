// Import required modules  
const express = require('express');  
const mysql = require('mysql2/promise');  
const bcrypt = require('bcryptjs');  
const bodyParser = require('body-parser');  
const cors = require('cors');  
const winston = require('winston');  
const { body, validationResult } = require('express-validator');  
const jwt = require('jsonwebtoken');  
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

// JWT Token Generation  
const generateToken = (user) => {  
    return jwt.sign(  
        { userId: user.user_id, email: user.email },  
        process.env.JWT_SECRET || 'your-secret-key',  
        { expiresIn: '24h' }  
    );  
};  

// Token Verification Middleware  
const verifyToken = (req, res, next) => {  
    const token = req.headers.authorization?.split(' ')[1];  
    if (!token) {  
        return res.status(401).json({ message: 'No token provided' });  
    }  

    try {  
        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key');  
        req.user = decoded;  
        next();  
    } catch (error) {  
        return res.status(401).json({ message: 'Invalid token' });  
    }  
};  

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

// User Registration Validation  
const registerValidation = [  
    body('name').notEmpty().withMessage('Name is required'),  
    body('email').isEmail().withMessage('Invalid email address'),  
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters long'),  
    body('phone_number').isMobilePhone().withMessage('Invalid phone number')  
];  

// Routes  

// 1. Register User  
app.post('/api/users/register', registerValidation, async (req, res) => {  
    const errors = validationResult(req);  
    if (!errors.isEmpty()) {  
        return res.status(400).json({ errors: errors.array() });  
    }  

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
    const token = generateToken(user);  
    logger.info('User logged in successfully:', { userId: user.user_id });  
    res.json({  
        message: 'Login successful',  
        user,  
        token  
    });  
});  

// 3. Product Endpoints  
// Get Product by ID  
app.get('/api/products/:id', async (req, res) => {  
    try {  
        const [product] = await pool.query(`  
            SELECT p.*,   
                   s.name as store_name,  
                   c.name as category_name,  
                   sc.name as subcategory_name  
            FROM products p  
            LEFT JOIN stores s ON p.store_id = s.store_id  
            LEFT JOIN category c ON p.category_id = c.category_id  
            LEFT JOIN subcategories sc ON p.subcategory_id = sc.subcategory_id  
            WHERE p.product_id = ?  
        `, [req.params.id]);  

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

// Create Product  
app.post('/api/products', verifyToken, async (req, res) => {  
    try {  
        const {   
            store_id,   
            name,   
            description,   
            price,   
            quantity,   
            category_id,   
            subcategory_id,  
            images   
        } = req.body;  

        // Verify store ownership  
        const [store] = await pool.query(  
            'SELECT * FROM stores WHERE store_id = ? AND owner_id = ?',  
            [store_id, req.user.userId]  
        );  

        if (store.length === 0) {  
            return res.status(403).json({ message: 'Unauthorized - Not store owner' });  
        }  

        const connection = await pool.getConnection();  
        try {  
            await connection.beginTransaction();  

            // Insert product  
            const [result] = await connection.query(  
                `INSERT INTO products (  
                    store_id, name, description, price, quantity,   
                    category_id, subcategory_id  
                ) VALUES (?, ?, ?, ?, ?, ?, ?)`,  
                [store_id, name, description, price, quantity, category_id, subcategory_id]  
            );  

            // Insert product images if provided  
            if (images && images.length > 0) {  
                const imageValues = images.map(url => [result.insertId, url]);  
                await connection.query(  
                    'INSERT INTO product_images (product_id, image_url) VALUES ?',  
                    [imageValues]  
                );  
            }  

            await connection.commit();  

            logger.info('Product created successfully:', { productId: result.insertId });  
            res.status(201).json({  
                message: 'Product created successfully',  
                productId: result.insertId  
            });  
        } catch (error) {  
            await connection.rollback();  
            throw error;  
        } finally {  
            connection.release();  
        }  
    } catch (error) {  
        logger.error('Error creating product:', error);  
        res.status(500).json({ message: 'Error creating product' });  
    }  
});  

// Update Product  
app.put('/api/products/:id', verifyToken, async (req, res) => {  
    try {  
        const {   
            name,   
            description,   
            price,   
            quantity,  
            category_id,  
            subcategory_id,  
            images   
        } = req.body;  

        // Verify product ownership  
        const [product] = await pool.query(`  
            SELECT p.* FROM products p  
            JOIN stores s ON p.store_id = s.store_id  
            WHERE p.product_id = ? AND s.owner_id = ?  
        `, [req.params.id, req.user.userId]);  

        if (product.length === 0) {  
            return res.status(403).json({ message: 'Unauthorized - Not product owner' });  
        }  

        const connection = await pool.getConnection();  
        try {  
            await connection.beginTransaction();  

            // Update product details  
            await connection.query(  
                `UPDATE products   
                 SET name = ?, description = ?, price = ?, quantity = ?,  
                     category_id = ?, subcategory_id = ?  
                 WHERE product_id = ?`,  
                [name, description, price, quantity, category_id, subcategory_id, req.params.id]  
            );  

            // Update images if provided  
            if (images) {  
                await connection.query(  
                    'DELETE FROM product_images WHERE product_id = ?',  
                    [req.params.id]  
                );  

                if (images.length > 0) {  
                    const imageValues = images.map(url => [req.params.id, url]);  
                    await connection.query(  
                        'INSERT INTO product_images (product_id, image_url) VALUES ?',  
                        [imageValues]  
                    );  
                }  
            }  

            await connection.commit();  
            res.json({ message: 'Product updated successfully' });  
        } catch (error) {  
            await connection.rollback();  
            throw error;  
        } finally {  
            connection.release();  
        }  
    } catch (error) {  
        logger.error('Error updating product:', error);  
        res.status(500).json({ message: 'Error updating product' });  
    }  
});  

// Cart Operations  
// Add to Cart  
app.post('/api/cart', verifyToken, async (req, res) => {  
    try {  
        const { product_id, quantity } = req.body;  
        const connection = await pool.getConnection();  

        try {  
            await connection.beginTransaction();  

            // Check if user has an active cart  
            let [cart] = await connection.query(  
                'SELECT cart_id FROM carts WHERE user_id = ? AND status = "active"',  
                [req.user.userId]  
            );  

            let cartId;  
            if (cart.length === 0) {  
                // Create new cart if none exists  
                const [newCart] = await connection.query(  
                    'INSERT INTO carts (user_id, status) VALUES (?, "active")',  
                    [req.user.userId]  
                );  
                cartId = newCart.insertId;  
            } else {  
                cartId = cart[0].cart_id;  
            }  

            // Check if product already in cart  
            const [existingItem] = await connection.query(  
                'SELECT * FROM cart_items WHERE cart_id = ? AND product_id = ?',  
                [cartId, product_id]  
            );  

            if (existingItem.length > 0) {  
                // Update quantity if product already in cart  
                await connection.query(  
                    'UPDATE cart_items SET quantity = quantity + ? WHERE cart_id = ? AND product_id = ?',  
                    [quantity, cartId, product_id]  
                );  
            } else {  
                // Add new item to cart  
                await connection.query(  
                    'INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)',  
                    [cartId, product_id, quantity]  
                );  
            }  

            await connection.commit();  
            res.status(201).json({ message: 'Item added to cart successfully' });  
        } catch (error) {  
            await connection.rollback();  
            throw error;  
        } finally {  
            connection.release();  
        }  
    } catch (error) {  
        logger.error('Error adding to cart:', error);  
        res.status(500).json({ message: 'Error adding to cart' });  
    }  
});  

// Order Operations  
// Create Order  
app.post('/api/orders', verifyToken, async (req, res) => {  
    const connection = await pool.getConnection();  
    try {  
        await connection.beginTransaction();  

        const { store_id, shipping_address, payment_method, items } = req.body;  

        // Calculate total price  
        let total_price = 0;  
        for (const item of items) {  
            const [product] = await connection.query(  
                'SELECT price FROM products WHERE product_id = ?',  
                [item.product_id]  
            );  
            total_price += product[0].price * item.quantity;  
        }  

        // Create order  
        const [order] = await connection.query(  
            `INSERT INTO orders (  
                user_id, store_id, total_price, shipping_address,   
                payment_method, status  
            ) VALUES (?, ?, ?, ?, ?, 'pending')`,  
            [req.user.userId, store_id, total_price, shipping_address, payment_method]  
        );  

        // Create order items  
        const orderItems = items.map(item => [  
            order.insertId,  
            item.product_id,  
            item.quantity  
        ]);  

        await connection.query(  
            'INSERT INTO order_items (order_id, product_id, quantity) VALUES ?',  
            [orderItems]  
        );  

        // Clear cart  
        await connection.query(  
            'DELETE FROM cart_items WHERE cart_id IN (SELECT cart_id FROM carts WHERE user_id = ?)',  
            [req.user.userId]  
        );  

        // Create notification  
        await connection.query(  
            `INSERT INTO notifications (user_id, type, message)   
             VALUES (?, 'order', ?)`,  
            [req.user.userId, `Order #${order.insertId} has been created successfully`]  
        );  

        await connection.commit();  
        res.status(201).json({  
            message: 'Order created successfully',  
            orderId: order.insertId  
        });  
    } catch (error) {  
        await connection.rollback();  
        logger.error('Error creating order:', error);  
        res.status(500).json({ message: 'Error creating order' });  
    } finally {  
        connection.release();  
    }  
});  

// Get Order History  
app.get('/api/orders', verifyToken, async (req, res) => {  
    try {  
        const [orders] = await pool.query(`  
            SELECT o.*,   
                   s.name as store_name,  
                   JSON_ARRAYAGG(  
                       JSON_OBJECT(  
                           'product_id', p.product_id,  
                           'name', p.name,  
                           'quantity', oi.quantity,  
                           'price', p.price  
                       )  
                   ) as items  
            FROM orders o  
            JOIN stores s ON o.store_id = s.store_id  
            JOIN order_items oi ON o.order_id = oi.order_id  
            JOIN products p ON oi.product_id = p.product_id  
            WHERE o.user_id = ?  
            GROUP BY o.order_id  
            ORDER BY o.created_at DESC  
        `, [req.user.userId]);  

        res.json(orders);  
    } catch (error) {  
        logger.error('Error fetching orders:', error);  
        res.status(500).json({ message: 'Error fetching orders' });  
    }  
});  

// Store Operations  
// Create Store  
app.post('/api/stores', verifyToken, async (req, res) => {  
    try {  
        const {   
            name,   
            description,   
            address,   
            phone_number,  
            business_type,  
            store_image   
        } = req.body;  

        const [result] = await pool.query(  
            `INSERT INTO stores (  
                owner_id, name, description, address,   
                phone_number, business_type, store_image  
            ) VALUES (?, ?, ?, ?, ?, ?, ?)`,  
            [req.user.userId, name, description, address, phone_number, business_type, store_image]  
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

// Update Store  
app.put('/api/stores/:id', verifyToken, async (req, res) => {  
    try {  
        const {   
            name,   
            description,   
            address,   
            phone_number,  
            business_type,  
            store_image   
        } = req.body;  

        // Verify store ownership  
        const [store] = await pool.query(  
            'SELECT * FROM stores WHERE store_id = ? AND owner_id = ?',  
            [req.params.id, req.user.userId]  
        );  

        if (store.length === 0) {  
            return res.status(403).json({ message: 'Unauthorized - Not store owner' });  
        }  

        await pool.query(  
            `UPDATE stores   
             SET name = ?, description = ?, address = ?,  
                 phone_number = ?, business_type = ?, store_image = ?  
             WHERE store_id = ?`,  
            [name, description, address, phone_number, business_type, store_image, req.params.id]  
        );  

        res.json({ message: 'Store updated successfully' });  
    } catch (error) {  
        logger.error('Error updating store:', error);  
        res.status(500).json({ message: 'Error updating store' });  
    }  
});  

// Review Operations  
// Add Review  
app.post('/api/reviews', verifyToken, async (req, res) => {  
    try {  
        const { store_id, rating, comment, images } = req.body;  

        const connection = await pool.getConnection();  
        try {  
            await connection.beginTransaction();  

            // Add review  
            const [review] = await connection.query(  
                `INSERT INTO reviews (  
                    user_id, store_id, rating, comment  
                ) VALUES (?, ?, ?, ?)`,  
                [req.user.userId, store_id, rating, comment]  
            );  

            // Add review images if provided  
            if (images && images.length > 0) {  
                const imageValues = images.map(url => [review.insertId, url]);  
                await connection.query(  
                    'INSERT INTO review_images (review_id, image_url) VALUES ?',  
                    [imageValues]  
                );  
            }  

            // Update store rating  
            await connection.query(  
                `UPDATE stores   
                 SET rating = (  
                     SELECT AVG(rating)   
                     FROM reviews   
                     WHERE store_id = ?  
                 )  
                 WHERE store_id = ?`,  
                [store_id, store_id]  
            );  

            await connection.commit();  
            res.status(201).json({  
                message: 'Review added successfully',  
                reviewId: review.insertId  
            });  
        } catch (error) {  
            await connection.rollback();  
            throw error;  
        } finally {  
            connection.release();  
        }  
    } catch (error) {  
        logger.error('Error adding review:', error);  
        res.status(500).json({ message: 'Error adding review' });  
    }  
});  

// Get Store Reviews  
app.get('/api/stores/:storeId/reviews', async (req, res) => {  
    try {  
        const [reviews] = await pool.query(`  
            SELECT r.*,  
                   u.name as reviewer_name,  
                   u.profile_image as reviewer_image,  
                   JSON_ARRAYAGG(ri.image_url) as review_images  
            FROM reviews r  
            JOIN users u ON r.user_id = u.user_id  
            LEFT JOIN review_images ri ON r.review_id = ri.review_id  
            WHERE r.store_id = ?  
            GROUP BY r.review_id  
            ORDER BY r.created_at DESC  
        `, [req.params.storeId]);  

        res.json(reviews);  
    } catch (error) {  
        logger.error('Error fetching reviews:', error);  
        res.status(500).json({ message: 'Error fetching reviews' });  
    }  
});  

// Notification Operations  
// Get User Notifications  
app.get('/api/notifications', verifyToken, async (req, res) => {  
    try {  
        const [notifications] = await pool.query(`  
            SELECT *  
            FROM notifications  
            WHERE user_id = ?  
            ORDER BY created_at DESC  
            LIMIT 50  
        `, [req.user.userId]);  

        // Mark notifications as read  
        await pool.query(`  
            UPDATE notifications  
            SET is_read = true  
            WHERE user_id = ? AND is_read = false  
        `, [req.user.userId]);  

        res.json(notifications);  
    } catch (error) {  
        logger.error('Error fetching notifications:', error);  
        res.status(500).json({ message: 'Error fetching notifications' });  
    }  
});  

// Search Operations  
// Advanced Search with Filters and Sorting  
app.get('/api/search', async (req, res) => {  
    try {  
        const {  
            query,  
            category_id,  
            min_price,  
            max_price,  
            rating,  
            sort_by,  
            sort_order,  
            page = 1,  
            limit = 20  
        } = req.query;  

        let sqlQuery = `  
            SELECT   
                p.*,  
                s.name as store_name,  
                s.rating as store_rating,  
                c.name as category_name,  
                COALESCE(  
                    (SELECT MIN(price)   
                     FROM product_variants   
                     WHERE product_id = p.product_id  
                    ),   
                    p.price  
                ) as min_variant_price  
            FROM products p  
            JOIN stores s ON p.store_id = s.store_id  
            JOIN category c ON p.category_id = c.category_id  
            WHERE 1=1  
        `;  

        const params = [];  

        if (query) {  
            sqlQuery += ` AND (p.name LIKE ? OR p.description LIKE ?)`;  
            params.push(`%${query}%`, `%${query}%`);  
        }  

        if (category_id) {  
            sqlQuery += ` AND p.category_id = ?`;  
            params.push(category_id);  
        }  

        if (min_price) {  
            sqlQuery += ` AND p.price >= ?`;  
            params.push(min_price);  
        }  

        if (max_price) {  
            sqlQuery += ` AND p.price <= ?`;  
            params.push(max_price);  
        }  

        if (rating) {  
            sqlQuery += ` AND s.rating >= ?`;  
            params.push(rating);  
        }  

        // Sorting  
        switch (sort_by) {  
            case 'price':  
                sqlQuery += ` ORDER BY min_variant_price ${sort_order || 'ASC'}`;  
                break;  
            case 'rating':  
                sqlQuery += ` ORDER BY s.rating ${sort_order || 'DESC'}`;  
                break;  
            case 'date':  
                sqlQuery += ` ORDER BY p.created_at ${sort_order || 'DESC'}`;  
                break;  
            default:  
                sqlQuery += ` ORDER BY p.created_at DESC`;  
        }  

        // Pagination  
        const offset = (page - 1) * limit;  
        sqlQuery += ` LIMIT ? OFFSET ?`;  
        params.push(parseInt(limit), offset);  

        const [products] = await pool.query(sqlQuery, params);  

        // Get total count for pagination  
        const [totalCount] = await pool.query(  
            `SELECT COUNT(*) as total FROM (${sqlQuery.split('LIMIT')[0]}) as subquery`,  
            params.slice(0, -2)  
        );  

        res.json({  
            products,  
            pagination: {  
                current_page: parseInt(page),  
                total_pages: Math.ceil(totalCount[0].total / limit),  
                total_items: totalCount[0].total  
            }  
        });  
    } catch (error) {  
        logger.error('Error searching products:', error);  
        res.status(500).json({ message: 'Error searching products' });  
    }  
});  

// Wishlist Operations  
// Add to Wishlist  
app.post('/api/wishlist', verifyToken, async (req, res) => {  
    try {  
        const { product_id } = req.body;  

        // Check if already in wishlist  
        const [existing] = await pool.query(  
            'SELECT * FROM wishlist WHERE user_id = ? AND product_id = ?',  
            [req.user.userId, product_id]  
        );  

        if (existing.length > 0) {  
            return res.status(400).json({ message: 'Product already in wishlist' });  
        }  

        await pool.query(  
            'INSERT INTO wishlist (user_id, product_id) VALUES (?, ?)',  
            [req.user.userId, product_id]  
        );  

        res.status(201).json({ message: 'Added to wishlist successfully' });  
    } catch (error) {  
        logger.error('Error adding to wishlist:', error);  
        res.status(500).json({ message: 'Error adding to wishlist' });  
    }  
});  

// Get Wishlist  
app.get('/api/wishlist', verifyToken, async (req, res) => {  
    try {  
        const [wishlist] = await pool.query(`  
            SELECT   
                w.*,  
                p.name,  
                p.description,  
                p.price,  
                p.image_url,  
                s.name as store_name  
            FROM wishlist w  
            JOIN products p ON w.product_id = p.product_id  
            JOIN stores s ON p.store_id = s.store_id  
            WHERE w.user_id = ?  
            ORDER BY w.created_at DESC  
        `, [req.user.userId]);  

        res.json(wishlist);  
    } catch (error) {  
        logger.error('Error fetching wishlist:', error);  
        res.status(500).json({ message: 'Error fetching wishlist' });  
    }  
});  

// Category and Subcategory Operations  
// Get Categories with Subcategories  
app.get('/api/categories', async (req, res) => {  
    try {  
        const [categories] = await pool.query(`  
            SELECT   
                c.*,  
                JSON_ARRAYAGG(  
                    JSON_OBJECT(  
                        'id', s.subcategory_id,  
                        'name', s.name,  
                        'description', s.description  
                    )  
                ) as subcategories  
            FROM category c  
            LEFT JOIN subcategories s ON c.category_id = s.category_id  
            GROUP BY c.category_id  
        `);  

        res.json(categories);  
    } catch (error) {  
        logger.error('Error fetching categories:', error);  
        res.status(500).json({ message: 'Error fetching categories' });  
    }  
});  

// Analytics and Reporting  
// Get Store Analytics  
app.get('/api/stores/:storeId/analytics', verifyToken, async (req, res) => {  
    try {  
        const { period = 'month' } = req.query;  

        // Verify store ownership  
        const [store] = await pool.query(  
            'SELECT * FROM stores WHERE store_id = ? AND owner_id = ?',  
            [req.params.storeId, req.user.userId]  
        );  

        if (store.length === 0) {  
            return res.status(403).json({ message: 'Unauthorized - Not store owner' });  
        }  

        const periodClause = period === 'week'   
            ? 'AND created_at >= DATE_SUB(NOW(), INTERVAL 1 WEEK)'  
            : 'AND created_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH)';  

        // Get sales data  
        const [sales] = await pool.query(`  
            SELECT   
                DATE(created_at) as date,  
                COUNT(*) as order_count,  
                SUM(total_price) as total_sales  
            FROM orders  
            WHERE store_id = ? ${periodClause}  
            GROUP BY DATE(created_at)  
            ORDER BY date  
        `, [req.params.storeId]);  

        // Get top products  
        const [topProducts] = await pool.query(`  
            SELECT   
                p.product_id,  
                p.name,  
                COUNT(*) as order_count,  
                SUM(oi.quantity) as total_quantity  
            FROM order_items oi  
            JOIN products p ON oi.product_id = p.product_id  
            JOIN orders o ON oi.order_id = o.order_id  
            WHERE o.store_id = ? ${periodClause}  
            GROUP BY p.product_id  
            ORDER BY order_count DESC  
            LIMIT 5  
        `, [req.params.storeId]);  

        res.json({  
            sales,  
            topProducts,  
            summary: {  
                total_orders: sales.reduce((acc, curr) => acc + curr.order_count, 0),  
                total_revenue: sales.reduce((acc, curr) => acc + curr.total_sales, 0)  
            }  
        });  
    } catch (error) {  
        logger.error('Error fetching analytics:', error);  
        res.status(500).json({ message: 'Error fetching analytics' });  
    }  
});  

// Error Handling Middleware  
app.use((err, req, res, next) => {  
    logger.error('Unhandled error:', err);  
    res.status(500).json({   
        message: 'Internal server error',  
        error: process.env.NODE_ENV === 'development' ? err.message : undefined  
    });  
});  

// Start Server  
app.listen(PORT, '0.0.0.0', () => {  
    logger.info(`Server running on port ${PORT}`);  
});  

module.exports = app;  
