# Thriftly - Cloud Computing Infrastructure

## Overview
This repository contains the backend infrastructure and API implementations for the Thriftly application, a modern e-commerce platform for thrift shopping. The system is built using Node.js and deployed on Google Cloud Platform (GCP).

## Architecture
![Architecture Diagram](thriftly-backend/src/architecture-diagram.png)

### Core Components
- **REST API Server**: Express.js-based API handling core business logic
- **Recommendation Engine**: TensorFlow.js-based collaborative filtering system
- **Database**: Cloud SQL (MySQL) for data persistence
- **Cloud Storage**: For storing product images and assets
- **Authentication**: JWT-based authentication system

## Tech Stack
- **Runtime**: Node.js 18.x
- **Framework**: Express.js 4.x
- **Database**: MySQL 8.0
- **ML Framework**: TensorFlow.js
- **Cloud Provider**: Google Cloud Platform

## API Documentation
The API is organized around REST principles. All requests and responses are in JSON format.

### Base URL
https://github.com/Thriftly-Sustainable-Fashion/Thriftly-Sustainable-Fashion/tree/main/thriftly-backend

### Key Endpoints
- `/api/users/*` - User management
- `/api/products/*` - Product operations
- `/api/orders/*` - Order processing
- `/api/recommendations/*` - ML-based recommendations
- Complete API documentation available in [API_DOCS.md](./docs/API_DOCS.md)


## Cloud Services Overview

### 1. **Artifact Registry**
   - **Service Display Name**: Artifact Registry
   - **Description**: A fully managed service for storing and managing container images and language packages.
   - **Usage**: 
     - **Quantity**: 5
     - **Region**: Global
     - **Service ID**: 149C-F9EC-3994
     - **SKU**: 8502-299A-ABAF
     - **Total Price**: \$0.45

### 2. **Cloud Run**
   - **Service Display Name**: Cloud Run
   - **Description**: A serverless platform that automatically scales your containerized applications.
   - **Usage**:
     - **CPU Allocation Time**: 3,750,000 units
     - **Memory Allocation Time**: 1,875,000 units
     - **Requests**: 1,000,000
     - **Region**: Global
     - **Service ID**: 152E-C115-5142
     - **Total Price**: \$0.09 (CPU) + \$0.00469 (Memory) + \$0 (Requests)

### 3. **Cloud SQL (MySQL)**
   - **Service Display Name**: Cloud SQL for MySQL
   - **Description**: A fully managed database service that makes it easy to set up, maintain, manage, and administer MySQL relational databases on Google Cloud.
   - **Usage**:
     - **Zonal - vCPU**: 2,920 units
     - **Zonal - RAM**: 11,680 units
     - **Zonal - Standard Storage**: 73,000 units
     - **Region**: asia-southeast2
     - **Service ID**: 9662-B51E-5089
     - **Total Price**: \$156.804 (vCPU) + \$106.288 (RAM) + \$22.1 (Storage)

### 4. **Cloud Logging (Cloud Operations)**
   - **Service Display Name**: Cloud Logging
   - **Description**: A service that allows you to store, search, analyze, monitor, and alert on log data and events from Google Cloud.
   - **Usage**:
     - **Log Storage Cost**: 5 units
     - **Log Retention Cost**: 10 units
     - **Total Price**: \$0 (Storage) + \$0.1 (Retention)

### 5. **Cloud Build**
   - **Service Display Name**: Cloud Build
   - **Description**: A service that executes your builds on Google Cloud infrastructure.
   - **Usage**:
     - **Build Time**: 10 units (e2-medium)
     - **SSD Disk for Build Time**: 2 units
     - **Total Price**: \$0.03 (Build Time) + \$0.34 (SSD Disk)

### 6. **Cloud Storage**
   - **Service Display Name**: Cloud Storage
   - **Description**: A unified object storage for developers and enterprises, from live data to archived data.
   - **Usage**:
     - **Standard Storage US Regional**: 10 units
     - **Region**: us-central1
     - **Total Price**: \$0.1

### **Total Monthly Cost**
- **Total Price**: \$287.13
- **Effective Date**: December 13, 2024

---

## Machine Learning and Model Deployment

### Overview of ML Features
The machine learning features in the Thriftly application are primarily focused on providing personalized recommendations to users based on their interactions and preferences. The following endpoints in the API facilitate this functionality:

### 1. **Model Loading and Prediction**
- **Endpoint**: `/api/predict`
- **Method**: POST
- **Description**: This endpoint accepts a user ID and a product ID to generate a recommendation score using a pre-trained collaborative filtering model.
- **Input Validation**: Ensures that the user ID and product ID fall within valid ranges.
- **Prediction Logic**: 
  - Converts user and product IDs into tensors.
  - Uses the loaded TensorFlow model to predict a score indicating the likelihood of the user liking the product.
  - Saves the prediction result to the database for future reference.

### 2. **Batch Prediction**
- **Endpoint**: `/api/predict-batch`
- **Method**: POST
- **Description**: Similar to the single prediction endpoint, but allows for batch processing of multiple user-product pairs.
- **Input Validation**: Checks that the input arrays are valid and of equal length.
- **Batch Processing Logic**: 
  - Converts arrays of user and product IDs into tensors.
  - Predicts scores for all pairs and saves them to the database in a single operation.

### 3. **Recommendations Retrieval**
- **Endpoint**: `/api/recommendations/:userId`
- **Method**: GET
- **Description**: Retrieves the top 10 product recommendations for a specific user based on previously generated scores.
- **Database Query**: Joins the recommendations table with the products table to provide additional product details (e.g., name, image).

### 4. **Health Check**
- **Endpoint**: `/api/health`
- **Method**: GET
- **Description**: A simple endpoint to check the health of the service and whether the ML model has been loaded successfully.

---

## Mobile API Integration

### Overview of Mobile API Features
The Thriftly mobile application interacts with the backend API to provide users with a seamless shopping experience. The following endpoints are particularly relevant for mobile clients:

### 1. **User Authentication**
- **Endpoints**:
  - **Register**: `/api/users/register` (POST)
  - **Login**: `/api/users/login` (POST)
- **Description**: These endpoints allow users to create accounts and log in to the application. They handle user credentials securely and return authentication tokens for session management.

### 2. **Product Management**
- **Endpoints**:
  - **Get All Products**: `/api/products` (GET)
  - **Get Product by ID**: `/api/products/:id` (GET)
  - **Create Product**: `/api/products` (POST)
  - **Update Product**: `/api/products/:id` (PUT)
- **Description**: These endpoints enable mobile users to browse products, view details, and manage their listings if they are sellers.

### 3. **Cart Operations**
- **Endpoints**:
  - **Add to Cart**: `/api/cart` (POST)
  - **Get Cart Items**: `/api/cart/:userId` (GET)
  - **Update Cart Item**: `/api/cart/:cartItemId` (PUT)
  - **Remove from Cart**: `/api/cart/:cartItemId` (DELETE)
- **Description**: These endpoints allow users to manage their shopping cart, including adding items, viewing current cart contents, updating quantities, and removing items.

### 4. **Order Management**
- **Endpoints**:
  - **Create Order**: `/api/orders` (POST)
  - **Get User Orders**: `/api/orders` (GET)
  - **Get Order by ID**: `/api/orders/:orderId` (GET)
  - **Update Order Status**: `/api/orders/:orderId` (PUT)
  - **Delete Order**: `/api/orders/:orderId` (DELETE)
- **Description**: These endpoints facilitate the order process, allowing users to create orders, view their order history, and manage order statuses.

### 5. **Wishlist Management**
- **Endpoints**:
  - **Add to Wishlist**: `/api/wishlist` (POST)
  - **Get User's Wishlist**: `/api/wishlist/:userId` (GET)
- **Description**: Users can save products to their wishlist for future reference and easily access them later.

---

## Contributing
1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Team Members
- [Member 1] - Cloud Infrastructure Engineer
- [Member 2] - Backend Developer
- [Member 3] - ML Engineer

## Contact
Project Link: https://github.com/Thriftly-Sustainable-Fashion/Thriftly-Sustainable-Fashion.git

---
© 2024 Thriftly. All Rights Reserved.
