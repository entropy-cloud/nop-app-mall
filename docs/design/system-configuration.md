# System Configuration Business Design

## Purpose

Describe the system configuration, file storage, notifications, and operational settings for nop-app-mall.

## System Configuration

### Overview

System configuration stores key-value pairs that control mall business behavior. These are modifiable by admin users through the management interface.

### Configuration Items

| Key | Default | Description |
|-----|---------|-------------|
| `mall_index_new` | 10 | Homepage new products display count |
| `mall_index_hot` | 10 | Homepage hot products display count |
| `mall_index_brand` | 10 | Homepage brands display count |
| `mall_index_topic` | 10 | Homepage topics display count |
| `mall_index_category` | 5 | Homepage categories display count |
| `mall_freight` | 8 | Default freight amount (yuan) |
| `mall_freight_free` | 88 | Free shipping threshold (yuan) |
| `mall_order_unpaid_timeout` | 30 | Order auto-cancel timeout (minutes) |
| `mall_order_confirm_timeout` | 7 | Order auto-confirm timeout (days) |
| `mall_order_comment_timeout` | 7 | Order comment time limit (days after receipt) |
| `mall_address_limit` | 20 | Max addresses per user |
| `mall_name` | nop-app-mall | Mall name |
| `mall_address` | - | Mall physical address |
| `mall_phone` | - | Mall contact phone |
| `mall_qq` | - | Mall QQ number |
| `mall_latitude` | - | Mall location latitude |
| `mall_longitude` | - | Mall location longitude |

### Business Rules

- Configuration keys are predefined; new keys require code change
- Values are stored as strings; parsing to appropriate type is application responsibility
- Changes take effect immediately (no restart required for most items)
- Timeout-related changes affect new orders only, not existing ones
- Super admin only can modify system configuration

### Implementation with Nop Platform

- Use Nop Platform's `nop-sys` module for system configuration
- Or define a dedicated `app_mall_system_config` entity
- BizModel provides `@BizQuery getConfig(key)` and `@BizMutation updateConfig(key, value)`
- Cache configuration values for performance; invalidate on update

## File Storage

### Overview

File storage handles image uploads for goods, brands, avatars, and other resources.

### Storage Model

- Fields: key (unique index), name, type (MIME type), size, url
- Supports multiple backends: local filesystem, Alibaba OSS, Tencent COS
- Backend selection via configuration

### MVP Implementation

- Local filesystem storage (default)
- Upload directory configurable
- File access via HTTP endpoint
- File key generated as UUID or hash-based unique identifier

### Deferred: Cloud Storage

- Alibaba OSS integration
- Tencent COS integration
- Backend abstraction: `StorageService` interface with multiple implementations

### Business Rules

- File size limits (configurable)
- Allowed file types: images (jpg, png, gif, webp), documents (deferred)
- File key must be unique
- Deletion is soft (mark deleted, retain file) or hard (remove file)
- File URLs must be accessible via HTTP

## Notifications (Deferred)

### Types

- **SMS notification**: payment success, shipping confirmation
- **WeChat notification**: order status changes (via WeChat template message)
- **Email notification**: new order to admin, order status changes
- **In-app notification**: system notices to users

### Implementation Notes

- Notification service as a cross-cutting concern in service layer
- Async delivery (do not block order processing)
- Template-based content generation
- Delivery status tracking

## Scheduled Tasks

### MVP Required Tasks

| Task | Frequency | Description |
|------|-----------|-------------|
| Order auto-cancel | Every 1 minute | Cancel unpaid orders past timeout |
| Order auto-confirm | Every 1 hour | Confirm shipped orders past timeout |

### Post-MVP Tasks (requires deferred features)

| Task | Frequency | Description | Depends On |
|------|-----------|-------------|------------|
| Coupon expiry | Every 1 hour | Mark expired coupons and user coupons | Coupon system |
| Groupon expiry | Every 1 hour | Mark expired groupon rules and failed groups | Groupon system |
| Comment expiry | Every 1 hour | Mark order goods past comment window (set comment=-1) | Comment system |

### Implementation with Nop Platform

- Use Nop Platform's task scheduling mechanism
- Or use Quarkus `@Scheduled` annotation for simple tasks
- Each task should be idempotent and handle concurrent execution safely
- Log task execution results

## Admin Operation Log

### Purpose

Record admin operations for audit trail and troubleshooting.

### Data Model

- Fields: admin, ip, type (operation category), action, status (success/fail), result, addition_info

### Logged Operations

- Product management: create, update, delete, on/off sale
- Order management: ship, refund, close
- User management: create, update, disable
- System management: configuration changes
- Marketing: coupon create, groupon create

### Implementation Notes

- Log via AOP or explicit logging in BizModel methods
- Include sufficient context (who, when, what, result)
- Log table should not be edited or deleted by normal operations
- Retention policy: configurable, default 90 days

## Data Statistics (Deferred)

### Dashboard Metrics

- Today's orders: count, revenue
- Pending orders: unpaid, unshipped
- Goods stats: total count, on-sale count
- User stats: total users, new today

### Order Statistics

- Time-series: daily/weekly/monthly order count and revenue
- Status distribution: pie chart of order statuses
- Top products: by sales count and revenue

### Product Statistics

- Sales ranking: top products by quantity sold
- Inventory alert: products with low stock

### Implementation Notes

- Aggregate queries against order and goods tables
- Consider materialized summary tables for performance if data grows
- Admin dashboard page in AMIS views

## Nop Platform Implementation Notes

- System configuration can leverage `nop-sys` module or custom entity
- File storage uses `StorageService` pattern with backend abstraction
- Scheduled tasks use Nop Platform task mechanism or Quarkus `@Scheduled`
- Admin logs via Nop Platform's built-in audit logging or custom log entity
- All configuration entities in `model/app-mall.orm.xml`
- Follow Nop convention: use `JsonTool` for JSON, `StringHelper` for strings, `CoreMetrics` for time

## Reference Projects Comparison

| Aspect | mall (macrozheng) | litemall | nop-app-mall |
|--------|-------------------|----------|-------------|
| Config storage | Hardcoded constants | Key-value system table | Nop sys or custom entity |
| File storage | Aliyun OSS only | Abstract (local/OSS/COS) | Abstract (local default) |
| Notifications | RabbitMQ-based | Core module (SMS/WX/mail) | Deferred |
| Scheduled tasks | RabbitMQ delayed messages | Spring @Scheduled | Quarkus @Scheduled |
| Admin logs | Operate history table | Log table | Deferred |
| Statistics | Dashboard with aggregation | Dashboard with aggregation | Deferred |
