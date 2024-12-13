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
app.post('/api/users/login', async (req, res) => {  
    try {
        const { email, password } = req.body;
        const [users] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);
        
        if (users.length === 0) {
            return res.status(401).json({ message: 'Invalid credentials' });
        }

        const user = users[0];
        const isValid = await bcrypt.compare(password, user.password_hash);
        
        if (!isValid) {
            return res.status(401).json({ message: 'Invalid credentials' });
        }

        const { password_hash, ...userWithoutPassword } = user;
        res.json({
            message: 'Login successful',
            user: userWithoutPassword
        });
    } catch (error) {
        logger.error('Login error:', error);
        res.status(500).json({ message: 'Login error' });
    }
});

// 3. Get All Products
app.get('/api/products', async (req, res) => {
    try {
        const [products] = await pool.query(
            `SELECT p.*, c.name as category_name, u.name as seller_name 
             FROM products p 
             LEFT JOIN category c ON p.category_id = c.category_id
             LEFT JOIN users u ON p.user_id = u.user_id
             WHERE p.deleted_at IS NULL`
        );
        res.json(products);
    } catch (error) {
        logger.error('Error fetching products:', error);
        res.status(500).json({ message: 'Error fetching products' });
    }
});

// 4. Get Product by ID
app.get('/api/products/:id', async (req, res) => {  
    try {  
        const [product] = await pool.query(
            `SELECT p.*, c.name as category_name, u.name as seller_name
             FROM products p
             LEFT JOIN category c ON p.category_id = c.category_id
             LEFT JOIN users u ON p.user_id = u.user_id
             WHERE p.product_id = ? AND p.deleted_at IS NULL`, 
            [req.params.id]
        );  
        if (product.length === 0) {  
            return res.status(404).json({ message: 'Product not found' });  
        }  
        res.json(product[0]);  
    } catch (error) {  
        logger.error('Error fetching product:', error);  
        res.status(500).json({ message: 'Error fetching product' });  
    }  
});

// 5. Create Product
app.post('/api/products', async (req, res) => {  
    try {  
        const { user_id, name, description, price, quantity, category_id, subcategory_id } = req.body;  
        const [result] = await pool.query(  
            'INSERT INTO products (user_id, name, description, price, quantity, category_id, subcategory_id) VALUES (?, ?, ?, ?, ?, ?, ?)',  
            [user_id, name, description, price, quantity, category_id, subcategory_id]  
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

// 6. Update Product
app.put('/api/products/:id', async (req, res) => {  
    try {  
        const { name, description, price, quantity, category_id, subcategory_id } = req.body;  
        const [result] = await pool.query(  
            'UPDATE products SET name = ?, description = ?, price = ?, quantity = ?, category_id = ?, subcategory_id = ? WHERE product_id = ?',  
            [name, description, price, quantity, category_id, subcategory_id, req.params.id]  
        );  
        if (result.affectedRows === 0) {  
            return res.status(404).json({ message: 'Product not found' });  
        }  
        res.json({ message: 'Product updated successfully' });  
    } catch (error) {  
        logger.error('Error updating product:', error);  
        res.status(500).json({ message: 'Error updating product' });  
    }  
});

// 7. Add to Cart
app.post('/api/cart', async (req, res) => {  
    try {  
        const { user_id, product_id, quantity } = req.body;  

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

        res.status(201).json({ message: 'Item added to cart successfully' });  
    } catch (error) {  
        logger.error('Error adding to cart:', error);  
        res.status(500).json({ message: 'Error adding to cart' });  
    }  
});

// 8. Get Cart Items
app.get('/api/cart/:userId', async (req, res) => {
    try {
        const [cartItems] = await pool.query(
            `SELECT ci.*, p.name, p.price, p.user_id as seller_id
             FROM carts c
             JOIN cart_items ci ON c.cart_id = ci.cart_id
             JOIN products p ON ci.product_id = p.product_id
             WHERE c.user_id = ?`,
            [req.params.userId]
        );
        res.json(cartItems);
    } catch (error) {
        logger.error('Error fetching cart items:', error);
        res.status(500).json({ message: 'Error fetching cart items' });
    }
});

// 9. Update Cart Item
app.put('/api/cart/:cartItemId', async (req, res) => {
    try {
        const { quantity } = req.body;
        const [result] = await pool.query(
            'UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?',
            [quantity, req.params.cartItemId]
        );
        if (result.affectedRows === 0) {
            return res.status(404).json({ message: 'Cart item not found' });
        }
        res.json({ message: 'Cart item updated successfully' });
    } catch (error) {
        logger.error('Error updating cart item:', error);
        res.status(500).json({ message: 'Error updating cart item' });
    }
});

// 10. Remove from Cart
app.delete('/api/cart/:cartItemId', async (req, res) => {
    try {
        const [result] = await pool.query(
            'DELETE FROM cart_items WHERE cart_item_id = ?',
            [req.params.cartItemId]
        );
        if (result.affectedRows === 0) {
            return res.status(404).json({ message: 'Cart item not found' });
        }
        res.json({ message: 'Item removed from cart successfully' });
    } catch (error) {
        logger.error('Error removing cart item:', error);
        res.status(500).json({ message: 'Error removing cart item' });
    }
});


// 11. Get User Orders
app.get('/api/orders', async (req, res) => {  
    try {  
        const { user_id } = req.query;
        const [orders] = await pool.query(  
            `SELECT o.*, oi.*, p.name as product_name   
             FROM orders o   
             LEFT JOIN order_items oi ON o.order_id = oi.order_id   
             LEFT JOIN products p ON oi.product_id = p.product_id   
             WHERE o.user_id = ? AND o.deleted_at IS NULL`,  
            [user_id]  
        );  
        res.json(orders);  
    } catch (error) {  
        logger.error('Error fetching orders:', error);  
        res.status(500).json({ message: 'Error fetching orders' });  
    }  
});  

// 12. Get Order by ID
app.get('/api/orders/:orderId', async (req, res) => {
    try {
        const [order] = await pool.query(
            `SELECT o.*, oi.*, p.name as product_name, u.name as seller_name
             FROM orders o
             JOIN order_items oi ON o.order_id = oi.order_id
             JOIN products p ON oi.product_id = p.product_id
             JOIN users u ON p.user_id = u.user_id
             WHERE o.order_id = ? AND o.deleted_at IS NULL`,
            [req.params.orderId]
        );
        
        if (order.length === 0) {
            return res.status(404).json({ message: 'Order not found' });
        }
        res.json(order);
    } catch (error) {
        logger.error('Error fetching order:', error);
        res.status(500).json({ message: 'Error fetching order' });
    }
});

// 13. Create Order
app.post('/api/orders', async (req, res) => {
    try {
        const { user_id, items, total_price } = req.body;
        
        // Start transaction
        const connection = await pool.getConnection();
        await connection.beginTransaction();

        try {
            // Create order
            const [order] = await connection.query(
                'INSERT INTO orders (user_id, total_price) VALUES (?, ?)',
                [user_id, total_price]
            );

            // Add order items
            for (const item of items) {
                await connection.query(
                    'INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)',
                    [order.insertId, item.product_id, item.quantity, item.price]
                );

                // Update product quantity
                await connection.query(
                    'UPDATE products SET quantity = quantity - ? WHERE product_id = ?',
                    [item.quantity, item.product_id]
                );
            }

            // Clear cart after successful order
            await connection.query(
                'DELETE ci FROM cart_items ci JOIN carts c ON ci.cart_id = c.cart_id WHERE c.user_id = ?',
                [user_id]
            );

            await connection.commit();
            res.status(201).json({
                message: 'Order created successfully',
                orderId: order.insertId
            });
        } catch (error) {
            await connection.rollback();
            throw error;
        } finally {
            connection.release();
        }
    } catch (error) {
        logger.error('Error creating order:', error);
        res.status(500).json({ message: 'Error creating order' });
    }
});

// 14. Update Order Status
app.put('/api/orders/:orderId', async (req, res) => {
    try {
        const { order_status } = req.body;
        const [result] = await pool.query(
            'UPDATE orders SET order_status = ? WHERE order_id = ?',
            [order_status, req.params.orderId]
        );
        
        if (result.affectedRows === 0) {
            return res.status(404).json({ message: 'Order not found' });
        }
        res.json({ message: 'Order status updated successfully' });
    } catch (error) {
        logger.error('Error updating order:', error);
        res.status(500).json({ message: 'Error updating order' });
    }
});

// 15. Delete Order (Soft Delete)
app.delete('/api/orders/:orderId', async (req, res) => {
    try {
        const [result] = await pool.query(
            'UPDATE orders SET deleted_at = CURRENT_TIMESTAMP WHERE order_id = ?',
            [req.params.orderId]
        );
        
        if (result.affectedRows === 0) {
            return res.status(404).json({ message: 'Order not found' });
        }
        res.json({ message: 'Order deleted successfully' });
    } catch (error) {
        logger.error('Error deleting order:', error);
        res.status(500).json({ message: 'Error deleting order' });
    }
});

// 16. Get User Recommendations
app.get('/api/recommendations/:userId', async (req, res) => {
    try {
        const userId = req.params.userId;
        const [recommendations] = await pool.query(
            `SELECT DISTINCT p.*, u.name as seller_name 
             FROM products p
             JOIN users u ON p.user_id = u.user_id
             JOIN category c ON p.category_id = c.category_id
             WHERE c.category_id IN (
                 SELECT DISTINCT p2.category_id
                 FROM orders o
                 JOIN order_items oi ON o.order_id = oi.order_id
                 JOIN products p2 ON oi.product_id = p2.product_id
                 WHERE o.user_id = ?
             )
             AND p.deleted_at IS NULL
             LIMIT 10`,
            [userId]
        );
        res.json(recommendations);
    } catch (error) {
        logger.error('Error fetching recommendations:', error);
        res.status(500).json({ message: 'Error fetching recommendations' });
    }
});

// 17. Get All Stores
app.get('/api/store', async (req, res) => {
    try {
        const [stores] = await pool.query(
            `SELECT u.user_id, u.store_name, u.store_description, u.store_address, 
                    u.store_created_at, u.store_number, u.store_email
             FROM users u 
             WHERE u.store_name IS NOT NULL AND u.deleted_at IS NULL`
        );
        res.json(stores);
    } catch (error) {
        logger.error('Error fetching stores:', error);
        res.status(500).json({ message: 'Error fetching stores' });
    }
});

// 18. Get Store by User ID
app.get('/api/store/:userId', async (req, res) => {
    try {
        const [store] = await pool.query(
            `SELECT u.user_id, u.store_name, u.store_description, u.store_address, 
                    u.store_created_at, u.store_number, u.store_email,
                    (SELECT COUNT(*) FROM products p WHERE p.user_id = u.user_id) as total_products,
                    (SELECT AVG(r.rating) FROM reviews r WHERE r.seller_id = u.user_id) as average_rating
             FROM users u 
             WHERE u.user_id = ? AND u.store_name IS NOT NULL AND u.deleted_at IS NULL`,
            [req.params.userId]
        );
        
        if (store.length === 0) {
            return res.status(404).json({ message: 'Store not found' });
        }
        res.json(store[0]);
    } catch (error) {
        logger.error('Error fetching store:', error);
        res.status(500).json({ message: 'Error fetching store' });
    }
});

// 19. Create Store Review
app.post('/api/reviews', async (req, res) => {
    try {
        const { user_id, seller_id, rating, comment } = req.body;

        if (rating < 1 || rating > 5) {
            return res.status(400).json({ message: 'Rating must be between 1 and 5' });
        }

        const [result] = await pool.query(
            'INSERT INTO reviews (user_id, seller_id, rating, comment) VALUES (?, ?, ?, ?)',
            [user_id, seller_id, rating, comment]
        );

        res.status(201).json({
            message: 'Review created successfully',
            reviewId: result.insertId
        });
    } catch (error) {
        logger.error('Error creating review:', error);
        res.status(500).json({ message: 'Error creating review' });
    }
});

// 20. Get Store Reviews
app.get('/api/store/:sellerId/reviews', async (req, res) => {
    try {
        const [reviews] = await pool.query(
            `SELECT r.*, u.name as reviewer_name
             FROM reviews r
             JOIN users u ON r.user_id = u.user_id
             WHERE r.seller_id = ?
             ORDER BY r.created_at DESC`,
            [req.params.sellerId]
        );
        res.json(reviews);
    } catch (error) {
        logger.error('Error fetching reviews:', error);
        res.status(500).json({ message: 'Error fetching reviews' });
    }
});

// 21. Upload Photo
app.post('/api/upload/photo', async (req, res) => {
    try {
        // Implement photo upload logic here
        // Example: Use multer for file upload and save the file path in the database
        res.status(201).json({ message: 'Photo uploaded successfully' });
    } catch (error) {
        logger.error('Error uploading photo:', error);
        res.status(500).json({ message: 'Error uploading photo' });
    }
});

// 22. Get Photo by ID
app.get('/api/photo/:photoId', async (req, res) => {
    try {
        // Implement logic to retrieve photo metadata or URL from the database
        res.json({ photo: 'photo_url' });
    } catch (error) {
        logger.error('Error fetching photo:', error);
        res.status(500).json({ message: 'Error fetching photo' });
    }
});

// 23. Delete Photo
app.delete('/api/photo/:photoId', async (req, res) => {
    try {
        // Implement logic to delete photo metadata and file from storage
        res.json({ message: 'Photo deleted successfully' });
    } catch (error) {
        logger.error('Error deleting photo:', error);
        res.status(500).json({ message: 'Error deleting photo' });
    }
});

// 24. Update Photo Metadata
app.put('/api/photo/:photoId/metadata', async (req, res) => {
    try {
        const { metadata } = req.body;
        // Implement logic to update photo metadata in the database
        res.json({ message: 'Photo metadata updated successfully' });
    } catch (error) {
        logger.error('Error updating photo metadata:', error);
        res.status(500).json({ message: 'Error updating photo metadata' });
    }
});

// 25. Add to Wishlist
app.post('/api/wishlist', async (req, res) => {
    try {
        const { user_id, product_id } = req.body;

        await pool.query(
            'INSERT INTO wishlist (user_id, product_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP',
            [user_id, product_id]
        );

        res.status(201).json({ message: 'Item added to wishlist successfully' });
    } catch (error) {
        logger.error('Error adding to wishlist:', error);
        res.status(500).json({ message: 'Error adding to wishlist' });
    }
});

// 26. Get User's Wishlist
app.get('/api/wishlist/:userId', async (req, res) => {
    try {
        const [wishlist] = await pool.query(
            `SELECT w.*, p.*, u.store_name
             FROM wishlist w
             JOIN products p ON w.product_id = p.product_id
             JOIN users u ON p.user_id = u.user_id
             WHERE w.user_id = ?`,
            [req.params.userId]
        );
        res.json(wishlist);
    } catch (error) {
        logger.error('Error fetching wishlist:', error);
        res.status(500).json({ message: 'Error fetching wishlist' });
    }
});

// 27. Get Categories with Subcategories
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

// 28. Search Products and Stores
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
                WHERE (p.name LIKE ? OR p.description LIKE ?)
            `;
            params.push(`%${query}%`, `%${query}%`);

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
                SELECT 'store' as type, u.user_id, u.store_name, u.store_description, u.store_address 
                FROM users u
                WHERE (u.store_name LIKE ? OR u.store_description LIKE ?)
            `;
            params.push(`%${query}%`, `%${query}%`);
        }

        const [results] = await pool.query(sql, params);

        res.json(results);
    } catch (error) {
        logger.error('Error performing search:', error);
        res.status(500).json({ message: 'Error performing search' });
    }
});

// 29. Get Store Analytics
app.get('/api/stores/:storeId/analytics', async (req, res) => {
    try {
        const store_id = req.params.storeId;

        // Verify store ownership
        const [store] = await pool.query(
            'SELECT * FROM users WHERE user_id = ? AND store_name IS NOT NULL',
            [store_id]
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
             WHERE user_id = ?`,
            [store_id]
        );

        const [productStats] = await pool.query(
            `SELECT 
                COUNT(*) as total_products,
                AVG(price) as average_price,
                SUM(quantity) as total_inventory
             FROM products 
             WHERE user_id = ?`,
            [store_id]
        );

        const [reviewStats] = await pool.query(
            `SELECT 
                COUNT(*) as total_reviews,
                AVG(rating) as average_rating
             FROM reviews 
             WHERE seller_id = ?`,
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


// Start Server  
app.listen(PORT, '0.0.0.0', () => {  
    logger.info(`Server running on port ${PORT}`);  
});  

module.exports = app;  
