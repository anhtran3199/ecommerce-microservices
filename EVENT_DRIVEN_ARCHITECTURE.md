# 🎯 Event-Driven Architecture Implementation

## 📋 Tổng quan

Project đã được refactor thành kiến trúc Event-Driven với 4 patterns chính:

1. **🐰 RabbitMQ Event Messaging** - Async communication
2. **🎭 Saga Pattern** - Distributed transactions
3. **🔄 CQRS Pattern** - Command/Query separation
4. **📚 Event Sourcing** - Event-based data storage

## 🏗️ Kiến trúc mới

### Trước (Synchronous):
```
User → Order Service → Product Service → Payment Service
              ↓
      Tight coupling, cascading failures
```

### Sau (Event-Driven):
```
1. User POST /api/orders → CreateOrderCommand
2. OrderAggregate.createOrder() → OrderCreatedEvent
3. Event → EventStore → RabbitMQ
4. OrderProcessingSaga → ReserveStockCommand
5. Product Service → StockReservedEvent
6. Saga → ProcessPaymentCommand
7. Payment Service → PaymentProcessedEvent
8. Saga → ConfirmOrderCommand
9. Order confirmed ✅
```

## 🚀 Cách chạy

### 1. Start Infrastructure
```bash
docker-compose up -d rabbitmq postgres-order postgres-product postgres-payment postgres-user redis
```

### 2. Start Services
```bash
# Build all
mvn clean compile

# Start Eureka
cd eureka-server && mvn spring-boot:run &

# Start API Gateway
cd api-gateway && mvn spring-boot:run &

# Start Services
cd user-service && mvn spring-boot:run &
cd product-service && mvn spring-boot:run &
cd order-service && mvn spring-boot:run &
cd payment-service && mvn spring-boot:run &
```

### 3. Test Event-Driven Flow

**Create Order (CQRS):**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "productId": 1,
    "quantity": 2,
    "totalAmount": 100.00
  }'
```

**Response:** `HTTP 202 Accepted` - "Order creation initiated"

## 📊 Monitoring

### RabbitMQ Management UI
- **URL:** http://localhost:15672
- **User:** admin / admin123
- **Queues:**
  - `order.events.queue`
  - `product.events.queue`
  - `payment.events.queue`
  - `saga.commands.queue`

### Event Store
```sql
-- View all events
SELECT * FROM event_store ORDER BY created_date;

-- View events for specific aggregate
SELECT * FROM event_store
WHERE aggregate_id = '123'
ORDER BY version;
```

### Saga Status
- Logs trong SagaManager
- In-memory active sagas map

## 🔧 Thành phần chính

### Common Module
```
common/
├── config/          # RabbitMQ, Jackson, Cache
├── cqrs/           # Command/Query buses
├── event/          # Event Sourcing framework
├── messaging/      # RabbitMQ publishers
├── saga/           # Saga orchestration
└── security/       # JWT shared components
```

### Order Service
```
order-service/
├── aggregate/      # OrderAggregate (Event Sourcing)
├── command/        # CQRS Commands & Handlers
├── event/          # Domain Events
├── listener/       # RabbitMQ Listeners
├── saga/           # OrderProcessingSaga
└── repository/     # Event-based repositories
```

## 💡 Lợi ích đạt được

### ✅ Microservices Best Practices
- **Loose Coupling:** Services không phụ thuộc trực tiếp
- **Service Autonomy:** Mỗi service quản lý data riêng
- **Fault Tolerance:** Failure isolation
- **Independent Scaling:** Scale từng service riêng

### ✅ Event-Driven Benefits
- **Async Processing:** Non-blocking operations
- **Event History:** Full audit trail
- **Eventual Consistency:** CAP theorem compliance
- **Extensibility:** Easy thêm services mới

### ✅ CQRS Benefits
- **Performance:** Optimized read/write models
- **Scalability:** Scale read/write separately
- **Flexibility:** Different storage for queries
- **Security:** Command validation

### ✅ Saga Benefits
- **Distributed Transactions:** Cross-service consistency
- **Compensation:** Rollback on failures
- **Resilience:** Handle service failures gracefully
- **Visibility:** Clear transaction flow

## 🎭 Saga Flow Example

### Order Processing Saga Steps:
1. **Order Created** → Reserve Stock Command
2. **Stock Reserved** → Process Payment Command
3. **Payment Processed** → Confirm Order Command
4. **Order Confirmed** → Saga Complete ✅

### Compensation Flow:
1. **Stock Reservation Failed** → Cancel Order
2. **Payment Failed** → Release Stock + Cancel Order

## 📝 Development Guidelines

### Thêm Command mới:
```java
// 1. Create Command
public class UpdateOrderCommand extends Command {
    private Long orderId;
    private String status;
}

// 2. Create Handler
@Service
public class UpdateOrderCommandHandler implements CommandHandler<UpdateOrderCommand> {
    public void handle(UpdateOrderCommand command) {
        // Business logic
    }
}
```

### Thêm Event mới:
```java
// 1. Create Event
public class OrderUpdatedEvent extends DomainEvent {
    private Long orderId;
    private String newStatus;
}

// 2. Update Aggregate
private void handle(OrderUpdatedEvent event) {
    this.status = OrderStatus.valueOf(event.getNewStatus());
}
```

### Thêm Saga Step:
```java
// Trong Saga class
private void handleNewEvent(NewEvent event) {
    setCurrentStep("NEW_STEP");

    SagaCommand command = new SagaCommand(
        "NewCommand", getSagaId(), "target-service", payload
    );

    addCommand(command);
}
```

## 🐛 Troubleshooting

### Build Issues
```bash
# Clean build
mvn clean compile

# Check dependencies
mvn dependency:tree
```

### RabbitMQ Issues
```bash
# Check connections
curl http://localhost:15672/api/connections

# Purge queues
curl -X DELETE http://localhost:15672/api/queues/%2F/order.events.queue/contents \
  -u admin:admin123
```

### Event Store Issues
```sql
-- Check event counts
SELECT aggregate_type, COUNT(*)
FROM event_store
GROUP BY aggregate_type;

-- Find duplicate events
SELECT event_id, COUNT(*)
FROM event_store
GROUP BY event_id
HAVING COUNT(*) > 1;
```

## 📈 Performance Tuning

### RabbitMQ
- Connection pooling
- Message prefetch
- Queue durability settings

### Event Store
- Index trên aggregate_id, event_type
- Partition by date
- Archive old events

### CQRS
- Read model caching
- Async projections
- Separate read databases

Kiến trúc này đã sẵn sàng để scale và handle complex business flows! 🚀