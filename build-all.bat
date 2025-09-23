@echo off
echo Building all microservices...

echo Building parent project...
mvn clean install -DskipTests

echo Building Eureka Server...
cd eureka-server
mvn clean package -DskipTests
cd ..

echo Building API Gateway...
cd api-gateway
mvn clean package -DskipTests
cd ..

echo Building User Service...
cd user-service
mvn clean package -DskipTests
cd ..

echo Building Product Service...
cd product-service
mvn clean package -DskipTests
cd ..

echo Building Order Service...
cd order-service
mvn clean package -DskipTests
cd ..

echo Building Payment Service...
cd payment-service
mvn clean package -DskipTests
cd ..

echo All services built successfully!