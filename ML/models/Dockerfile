# Use Node.js TensorFlow image  
FROM tensorflow/tensorflow:latest-jupyter  

# Install Node.js  
RUN apt-get update && apt-get install -y curl  
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash -  
RUN apt-get install -y nodejs  

# Set working directory  
WORKDIR /usr/src/app  

# Copy package files  
COPY package*.json ./  

# Install dependencies  
RUN npm install  

# Copy application files  
COPY . .  

# Expose port  
EXPOSE 8080  

# Start application  
CMD ["node", "loadModel.js"]  
