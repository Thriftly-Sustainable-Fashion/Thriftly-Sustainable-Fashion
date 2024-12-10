# Use the official Node.js image as the base image  
FROM node:18  

# Set the working directory inside the container  
WORKDIR /usr/src/app  

# Copy package.json and package-lock.json to the working directory  
COPY ML/package*.json ./  

# Install dependencies  
RUN npm install  

# Copy the rest of the application code to the working directory  
COPY ML/ .  

# Expose the port the app runs on  
EXPOSE 8080  

# Start the application  
CMD ["node", "loadModel.js"]  
