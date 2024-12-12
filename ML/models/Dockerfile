# Use Node.js 18 image  
FROM node:18  

# Set working directory  
WORKDIR /usr/src/app  

# Copy package files  
COPY package*.json ./  

# Install dependencies  
RUN npm install  

# Copy application code  
COPY . .  

# Expose port  
EXPOSE 8080  

# Start application  
CMD ["npm", "start"]  
