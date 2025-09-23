#!/bin/bash

echo "Building all microservices..."

# Build parent project
echo "Building parent project..."
mvn clean install -DskipTests

# Build each service
echo "Building common module..."
cd common && mvn clean install -DskipTests && cd ..

echo "Building eureka-server..."
cd eureka-server && mvn clean package -DskipTests && cd ..

echo "Building api-gateway..."
cd api-gateway && mvn clean package -DskipTests && cd ..

echo "Building user-service..."
cd user-service && mvn clean package -DskipTests && cd ..

echo "Building product-service..."
cd product-service && mvn clean package -DskipTests && cd ..

echo "Building order-service..."
cd order-service && mvn clean package -DskipTests && cd ..

echo "Building payment-service..."
cd payment-service && mvn clean package -DskipTests && cd ..

echo "All services built successfully!"
echo "Starting Docker Compose..."

# Start docker compose
docker-compose up -d --build

echo "All services are starting up!"
echo "Check logs with: docker-compose logs -f [service-name]"