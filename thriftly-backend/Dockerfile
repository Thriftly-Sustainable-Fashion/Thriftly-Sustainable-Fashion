FROM node:18-slim  

WORKDIR /usr/src/app  

COPY package*.json ./  

ENV NODE_ENV=production  

RUN npm ci --only=production  

COPY . .  

EXPOSE 8080  

CMD ["npm", "start"]  
