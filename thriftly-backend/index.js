const express = require('express');  
const mysql = require('mysql2/promise');  
const bcrypt = require('bcryptjs');  
const bodyParser = require('body-parser');  
const cors = require('cors');  
const jwt = require('jsonwebtoken');  
require('dotenv').config();  

const app = express();  
const PORT = process.env.PORT || 3000;  

// Middleware  
app.use(cors());  
app.use(bodyParser.json());  

// Database Connection Pool  
const pool = mysql.createPool({  
    host: process.env.DB_HOST || 'localhost',  
    user: process.env.DB_USER || 'root',  
    password: process.env.DB_PASSWORD || '',  
    database: process.env.DB_NAME || 'thriftly',  
    waitForConnections: true,  
    connectionLimit: 10,  
    queueLimit: 0  
});  

// JWT Token Generator  
const generateToken = (user) => {  
    return jwt.sign(  
        { userId: user.user_id, email: user.email },  
        process.env.JWT_SECRET || 'x/7Yp8B<MKrcQeh=P%)*y',  
        { expiresIn: '1h' }  
    );  
};  

// JWT Authentication Middleware  
const verifyToken = (req, res, next) => {  
    const token = req.headers.authorization?.split(' ')[1];  
    if (!token) {  
        return res.status(401).json({ message: 'Unauthorized' });  
    }  
    try {  
        const decoded = jwt.verify(token, process.env.JWT_SECRET || 'x/7Yp8B<MKrcQeh=P%)*y');  
        req.user = decoded;  
        next();  
    } catch (error) {  
        res.status(401).json({ message: 'Invalid token' });  
    }  
};  


const logger = winston.createLogger({  
    level: 'info',  
    format: winston.format.json(),  
    transports: [  
        new winston.transports.Console(),  
        new winston.transports.File({ filename: 'error.log', level: 'error' })  
    ]  
});  


app.post('/api/users/register', [  
    body('name').notEmpty().withMessage('Name is required'),  
    body('email').isEmail().withMessage('Invalid email address'),  
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters long'),  
    body('phone_number').isMobilePhone().withMessage('Invalid phone number')  
], async (req, res) => {  
    const errors = validationResult(req);  
    if (!errors.isEmpty()) {  
        return res.status(400).json({ errors: errors.array() });  
    }  

    try {  
        const { name, email, password, phone_number } = req.body;  
        const [existing] = await pool.query('SELECT * FROM users WHERE email = ?', [email]);  
        if (existing.length > 0) {  
            return res.status(400).json({ message: 'Email already registered' });  
        }  
        const password_hash = await bcrypt.hash(password, 10);  
        const [result] = await pool.query(  
            'INSERT INTO users (name, email, password_hash, phone_number) VALUES (?, ?, ?, ?)',  
            [name, email, password_hash, phone_number]  
        );  
        res.status(201).json({  
            message: 'User registered successfully',  
            userId: result.insertId  
        });  
    } catch (error) {  
        res.status(500).json({ message: 'Registration error' });  
    }  
});  

// 2. Login User  
app.post('/api/users/login', authenticateUser, (req, res) => {  
    const { password_hash, ...user } = req.user;  
    const token = generateToken(user);  
    res.json({  
        message: 'Login successful',  
        user,  
        token  
    });  
});  

// 3. Get Product by ID  
app.get('/api/products/:id', async (req, res) => {  
    try {  
        const [product] = await pool.query('SELECT * FROM products WHERE product_id = ?', [req.params.id]);  
        if (product.length === 0) {  
            return res.status(404).json({ message: 'Product not found' });  
        }  
        res.json(product[0]);  
    } catch (error) {  
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
        res.status(201).json({  
            message: 'Product created successfully',  
            productId: result.insertId  
        });  
    } catch (error) {  
        res.status(500).json({ message: 'Error creating product' });  
    }  
});  

// 5. Update Product  
app.put('/api/products/:id', authenticateUser, async (req, res) => {  
    try {  
        const { name, description, price, quantity } = req.body;  
        await pool.query(  
            'UPDATE products SET name = ?, description = ?, price = ?, quantity = ? WHERE product_id = ?',  
            [name, description, price, quantity, req.params.id]  
        );  
        res.json({ message: 'Product updated successfully' });  
    } catch (error) {  
        res.status(500).json({ message: 'Error updating product' });  
    }  
});  

// 6. Delete Product  
app.delete('/api/products/:id', authenticateUser, async (req, res) => {  
    try {  
        await pool.query('DELETE FROM products WHERE product_id = ?', [req.params.id]);  
        res.json({ message: 'Product deleted successfully' });  
    } catch (error) {  
        res.status(500).json({ message: 'Error deleting product' });  
    }  
});  

// 7. Get Cart  
app.get('/api/cart', verifyToken, async (req, res) => {  
    try {  
        const [cartItems] = await pool.query(  
            'SELECT ci.*, p.name, p.price FROM cart_items ci JOIN products p ON ci.product_id = p.product_id WHERE ci.cart_id IN (SELECT cart_id FROM carts WHERE user_id = ?)',  
            [req.user.userId]  
        );  
        res.json(cartItems);  
    } catch (error) {  
        res.status(500).json({ message: 'Error fetching cart' });  
    }  
});  

// 8. Delete Cart Item  
app.delete('/api/cart/:itemId', authenticateUser, async (req, res) => {  
    try {  
        await pool.query('DELETE FROM cart_items WHERE cart_item_id = ?', [req.params.itemId]);  
        res.json({ message: 'Item removed from cart successfully' });  
    } catch (error) {  
        res.status(500).json({ message: 'Error removing item from cart' });  
    }  
});  

// 9. Create Order  
app.post('/api/order', authenticateUser, async (req, res) => {  
    try {  
        const { store_id, total_price } = req.body;  
        const [result] = await pool.query(  
            'INSERT INTO orders (user_id, store_id, total_price) VALUES (?, ?, ?)',  
            [req.user.user_id, store_id, total_price]  
        );  
        res.status(201).json({  
            message: 'Order created successfully',  
            orderId: result.insertId  
        });  
    } catch (error) {  
        res.status(500).json({ message: 'Error creating order' });  
    }  
});  

// 10. Get Store by ID  
app.get('/api/store/:storeId', async (req, res) => {  
    try {  
        const [store] = await pool.query('SELECT * FROM stores WHERE store_id = ?', [req.params.storeId]);  
        if (store.length === 0) {  
            return res.status(404).json({ message: 'Store not found' });  
        }  
        res.json(store[0]);  
    } catch (error) {  
        res.status(500).json({ message: 'Error fetching store' });  
    }  
});  

// 11. Get Store by Owner ID  
app.get('/api/store/owner/:ownerId', async (req, res) => {  
    try {  
        const [stores] = await pool.query('SELECT * FROM stores WHERE owner_id = ?', [req.params.ownerId]);  
        res.json(stores);  
    } catch (error) {  
        res.status(500).json({ message: 'Error fetching stores' });  
    }  
});  

// 12. Create Store  
app.post('/api/store', authenticateUser, async (req, res) => {  
    try {  
        const { name, description, address } = req.body;  
        const [result] = await pool.query(  
            'INSERT INTO stores (owner_id, name, description, address) VALUES (?, ?, ?, ?)',  
            [req.user.user_id, name, description, address]  
        );  
        res.status(201).json({  
            message: 'Store created successfully',  
            storeId: result.insertId  
        });  
    } catch (error) {  
        res.status(500).json({ message: 'Error creating store' });  
    }  
});  

// 13. Delete User  
app.delete('/api/users/:id', authenticateUser, async (req, res) => {  
    try {  
        const userId = req.params.id;  
  
        // Security check - users can only delete their own account  
        if (req.user.user_id !== parseInt(userId)) {  
            return res.status(403).json({ message: 'Unauthorized - Can only delete your own account' });  
        }  
  
        // Transaction to ensure all related data is deleted  
        const connection = await pool.getConnection();  
        await connection.beginTransaction();  
  
        try {  
            // Delete all related data in correct order  
            await connection.query('DELETE FROM cart_items WHERE cart_id IN (SELECT cart_id FROM carts WHERE user_id = ?)', [userId]);  
            await connection.query('DELETE FROM carts WHERE user_id = ?', [userId]);  
            await connection.query('DELETE FROM orders WHERE user_id = ?', [userId]);  
            await connection.query('DELETE FROM stores WHERE owner_id = ?', [userId]);  
            await connection.query('DELETE FROM users WHERE user_id = ?', [userId]);  
  
            await connection.commit();  
  
            res.json({  
                message: 'User and all related data deleted successfully',  
                userId: userId  
            });  
        } catch (err) {  
            await connection.rollback();  
            throw err;  
        } finally {  
            connection.release();  
        }  
    } catch (error) {  
        console.error('Error deleting user:', error);  
        res.status(500).json({ message: 'Error deleting user' });  
    }  
});      

// CATEGORIES & SUBCATEGORIES ENDPOINTS  
// Get All Categories with Subcategories  
app.get('/api/categories', async (req, res) => {  
    try {  
        const [categories] = await pool.query(`  
            SELECT c.*,   
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
        res.status(500).json({ message: 'Error fetching categories' });  
    }  
});  

// WISHLIST ENDPOINTS  
// Get User's Wishlist  
app.get('/api/wishlist', authenticateUser, async (req, res) => {  
    try {  
        const [wishlist] = await pool.query(`  
            SELECT w.*, p.name, p.price, p.description, s.name as store_name  
            FROM wishlist w  
            JOIN products p ON w.product_id = p.product_id  
            JOIN stores s ON p.store_id = s.store_id  
            WHERE w.user_id = ?  
        `, [req.user.user_id]);  
        res.json(wishlist);  
    } catch (error) {  
        res.status(500).json({ message: 'Error fetching wishlist' });  
    }  
});  

// Add to Wishlist  
app.post('/api/wishlist', authenticateUser, async (req, res) => {  
    try {  
        const { product_id } = req.body;  
        await pool.query(  
            'INSERT INTO wishlist (user_id, product_id) VALUES (?, ?)',  
            [req.user.user_id, product_id]  
        );  
        res.status(201).json({ message: 'Added to wishlist successfully' });  
    } catch (error) {  
        res.status(500).json({ message: 'Error adding to wishlist' });  
    }  
});  

// Remove from Wishlist  
app.delete('/api/wishlist/:productId', authenticateUser, async (req, res) => {  
    try {  
        await pool.query(  
            'DELETE FROM wishlist WHERE user_id = ? AND product_id = ?',  
            [req.user.user_id, req.params.productId]  
        );  
        res.json({ message: 'Removed from wishlist successfully' });  
    } catch (error) {  
        res.status(500).json({ message: 'Error removing from wishlist' });  
    }  
});  

// REVIEWS ENDPOINTS  
// Get Store Reviews  
app.get('/api/stores/:storeId/reviews', async (req, res) => {  
    try {  
        const [reviews] = await pool.query(`  
            SELECT r.*, u.name as reviewer_name  
            FROM reviews r  
            JOIN users u ON r.user_id = u.user_id  
            WHERE r.store_id = ?  
            ORDER BY r.created_at DESC  
        `, [req.params.storeId]);  
        res.json(reviews);  
    } catch (error) {  
        res.status(500).json({ message: 'Error fetching reviews' });  
    }  
});  

// Add Review  
app.post('/api/stores/:storeId/reviews', authenticateUser, async (req, res) => {  
    try {  
        const { rating, comment } = req.body;  
        const [result] = await pool.query(  
            'INSERT INTO reviews (store_id, user_id, rating, comment) VALUES (?, ?, ?, ?)',  
            [req.params.storeId, req.user.user_id, rating, comment]  
        );  
        res.status(201).json({  
            message: 'Review added successfully',  
            reviewId: result.insertId  
        });  
    } catch (error) {  
        res.status(500).json({ message: 'Error adding review' });  
    }  
});  

// NOTIFICATIONS ENDPOINTS  
// Get User's Notifications  
app.get('/api/notifications', authenticateUser, async (req, res) => {  
    try {  
        const [notifications] = await pool.query(`  
            SELECT *  
            FROM notifications  
            WHERE user_id = ?  
            ORDER BY created_at DESC  
        `, [req.user.user_id]);  
        res.json(notifications);  
    } catch (error) {  
        res.status(500).json({ message: 'Error fetching notifications' });  
    }  
});  

// Mark Notification as Read  
app.patch('/api/notifications/:notificationId', authenticateUser, async (req, res) => {  
    try {  
        await pool.query(  
            'UPDATE notifications SET status = "read" WHERE notification_id = ? AND user_id = ?',  
            [req.params.notificationId, req.user.user_id]  
        );  
        res.json({ message: 'Notification marked as read' });  
    } catch (error) {  
        res.status(500).json({ message: 'Error updating notification' });  
    }  
});  

// SEARCH ENDPOINTS  
// Search Products with Filters  
app.get('/api/search', async (req, res) => {  
    try {  
        const { query, category, minPrice, maxPrice, sort } = req.query;  
        let sqlQuery = `  
            SELECT p.*, c.name as category_name, s.name as store_name,  
                   COALESCE(pd.discount_value, 0) as discount  
            FROM products p  
            JOIN category c ON p.category_id = c.category_id  
            JOIN stores s ON p.store_id = s.store_id  
            LEFT JOIN product_discounts pd ON p.product_id = pd.product_id  
            WHERE 1=1  
        `;  
        const params = [];  

        if (query) {  
            sqlQuery += ` AND (p.name LIKE ? OR p.description LIKE ?)`;  
            params.push(`%${query}%`, `%${query}%`);  
        }  

        if (category) {  
            sqlQuery += ` AND c.category_id = ?`;  
            params.push(category);  
        }  

        if (minPrice) {  
            sqlQuery += ` AND p.price >= ?`;  
            params.push(minPrice);  
        }  

        if (maxPrice) {  
            sqlQuery += ` AND p.price <= ?`;  
            params.push(maxPrice);  
        }  

        if (sort === 'price_asc') {  
            sqlQuery += ` ORDER BY p.price ASC`;  
        } else if (sort === 'price_desc') {  
            sqlQuery += ` ORDER BY p.price DESC`;  
        } else {  
            sqlQuery += ` ORDER BY p.created_at DESC`;  
        }  

        const [products] = await pool.query(sqlQuery, params);  

        // Log search if user is authenticated  
        if (req.user) {  
            await pool.query(  
                'INSERT INTO search_history (user_id, search_query) VALUES (?, ?)',  
                [req.user.user_id, query]  
            );  
        }  

        res.json(products);  
    } catch (error) {  
        res.status(500).json({ message: 'Error searching products' });  
    }  
});  

// VOUCHERS ENDPOINTS  
// Get Available Vouchers  
app.get('/api/vouchers', authenticateUser, async (req, res) => {  
    try {  
        const [vouchers] = await pool.query(`  
            SELECT v.*,  
                   COUNT(DISTINCT uv.id) as times_used,  
                   EXISTS(  
                       SELECT 1   
                       FROM user_vouchers uv2   
                       WHERE uv2.voucher_id = v.voucher_id   
                       AND uv2.user_id = ?  
                   ) as is_claimed  
            FROM vouchers v  
            LEFT JOIN user_vouchers uv ON v.voucher_id = uv.voucher_id  
            WHERE v.end_date >= CURDATE()  
            GROUP BY v.voucher_id  
        `, [req.user.user_id]);  
        res.json(vouchers);  
    } catch (error) {  
        res.status(500).json({ message: 'Error fetching vouchers' });  
    }  
});  

// Claim Voucher  
app.post('/api/vouchers/:voucherId/claim', authenticateUser, async (req, res) => {  
    const connection = await pool.getConnection();  
    try {  
        await connection.beginTransaction();  

        // Check if voucher exists and is valid  
        const [voucher] = await connection.query(  
            'SELECT * FROM vouchers WHERE voucher_id = ? AND end_date >= CURDATE()',  
            [req.params.voucherId]  
        );  

        if (voucher.length === 0) {  
            throw new Error('Voucher not found or expired');  
        }  

        // Check if user already claimed this voucher  
        const [existing] = await connection.query(  
            'SELECT * FROM user_vouchers WHERE user_id = ? AND voucher_id = ?',  
            [req.user.user_id, req.params.voucherId]  
        );  

        if (existing.length > 0) {  
            throw new Error('Voucher already claimed');  
        }  

        // Claim the voucher  
        await connection.query(  
            'INSERT INTO user_vouchers (user_id, voucher_id) VALUES (?, ?)',  
            [req.user.user_id, req.params.voucherId]  
        );  

        await connection.commit();  
        res.json({ message: 'Voucher claimed successfully' });  
    } catch (error) {  
        await connection.rollback();  
        res.status(400).json({ message: error.message });  
    } finally {  
        connection.release();  
    }  
});  

// Error Handling  
app.use((err, req, res, next) => {  
    console.error(err.stack);  
    res.status(500).json({ message: 'Internal server error' });  
});  

// Start Server  
app.listen(PORT, () => {  
    console.log(`Server running on port ${PORT}`);  
});  

module.exports = app;  