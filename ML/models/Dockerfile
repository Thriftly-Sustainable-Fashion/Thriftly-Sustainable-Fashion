# Use Node.js 18 slim image  
FROM node:18-slim  

# Install required system dependencies  
RUN apt-get update && apt-get install -y \  
    python3 \  
    build-essential \  
    && rm -rf /var/lib/apt/lists/*  

# Set working directory  
WORKDIR /app  

# Copy package files  
COPY package*.json ./  

# Install dependencies  
RUN npm ci --only=production  

# Copy application files  
COPY . .  

# Create directory for logs  
RUN mkdir -p /app/logs  

# Set permissions  
RUN chmod -R 755 /app  

# Expose port  
EXPOSE 8080  

# Healthcheck  
HEALTHCHECK --interval=30s --timeout=3s \  
    CMD curl -f http://localhost:8080/health || exit 1  

# Start application  
CMD ["node", "loadModel.js"]  
