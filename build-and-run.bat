@echo off
echo Building all microservices...

echo Building parent project...
call mvn clean install -DskipTests

echo Building common module...
cd common
call mvn clean install -DskipTests
cd ..

echo Building eureka-server...
cd eureka-server
call mvn clean package -DskipTests
cd ..

echo Building api-gateway...
cd api-gateway
call mvn clean package -DskipTests
cd ..

echo Building user-service...
cd user-service
call mvn clean package -DskipTests
cd ..

echo Building product-service...
cd product-service
call mvn clean package -DskipTests
cd ..

echo Building order-service...
cd order-service
call mvn clean package -DskipTests
cd ..

echo Building payment-service...
cd payment-service
call mvn clean package -DskipTests
cd ..

echo All services built successfully!
echo Starting Docker Compose...

docker-compose up -d --build

echo All services are starting up!
echo Check logs with: docker-compose logs -f [service-name]

pause