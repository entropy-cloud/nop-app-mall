# Feature Inventory

## Purpose

Track the stable feature map for the application.

## Features

| Feature              | Status     | Owner Doc                          | Requirement Source                     | Notes                          |
| -------------------- | ---------- | ---------------------------------- | -------------------------------------- | ------------------------------ |
| User management      | designed   | `docs/design/user-and-address.md`  | `docs/input/litemall-requirements.md` | Registration, login, profile   |
| Admin management     | designed   | `docs/design/user-and-address.md`  | `docs/input/litemall-requirements.md` | CRUD, role assignment          |
| Product management   | designed   | `docs/design/product-catalog.md`   | `docs/input/litemall-requirements.md` | Category, brand, goods, SKU    |
| Cart                 | designed   | `docs/design/order-and-cart.md`    | `docs/input/litemall-requirements.md` | Add, remove, quantity          |
| Order management     | designed   | `docs/design/order-and-cart.md`    | `docs/input/litemall-requirements.md` | Full order lifecycle           |
| Search               | designed   | `docs/design/product-catalog.md`   | `docs/input/litemall-requirements.md` | Search history, keywords       |
| Address management   | designed   | `docs/design/user-and-address.md`  | `docs/input/litemall-requirements.md` | CRUD, default, region          |
| System configuration | designed   | `docs/design/system-configuration.md` | `docs/input/litemall-requirements.md` | Config key-value, file storage |
| Payment (WeChat Pay) | deferred   | `docs/design/order-and-cart.md`    | `docs/requirements/mvp.md`            | Deferred in MVP; wx module exists |
| Coupon               | deferred   | `docs/design/marketing-and-promotions.md` | `docs/requirements/mvp.md`    | Deferred in MVP                |
| Group buying         | deferred   | `docs/design/marketing-and-promotions.md` | `docs/requirements/mvp.md`    | Deferred in MVP                |
| Comment/Review       | deferred   | `docs/design/order-and-cart.md`    | `docs/requirements/mvp.md`            | Deferred in MVP                |
| After-sales          | deferred   | `docs/design/order-and-cart.md`    | `docs/requirements/mvp.md`            | Deferred in MVP                |
| Content management   | deferred   | `docs/design/marketing-and-promotions.md` | `docs/requirements/mvp.md`    | Topics, ads, issues, notices   |
| Notifications        | deferred   | `docs/design/system-configuration.md` | `docs/requirements/mvp.md`         | SMS, WeChat, email             |
| Statistics           | deferred   | `docs/design/system-configuration.md` | `docs/requirements/mvp.md`         | Order, product, user stats     |
| Search history       | deferred   | `docs/design/marketing-and-promotions.md` | `docs/requirements/mvp.md`    | Keyword history, clear         |
| Favorites            | deferred   | `docs/design/marketing-and-promotions.md` | `docs/requirements/mvp.md`    | Goods and topic favorites      |
| Browse footprint     | deferred   | `docs/design/marketing-and-promotions.md` | `docs/requirements/mvp.md`    | Browsing history               |
| Feedback             | deferred   | `docs/design/marketing-and-promotions.md` | `docs/requirements/mvp.md`    | User feedback submission       |
| FAQ (Issue)          | deferred   | `docs/design/marketing-and-promotions.md` | `docs/requirements/mvp.md`    | Help center FAQ management     |
| Notice               | deferred   | `docs/design/system-configuration.md` | `docs/requirements/mvp.md`         | System and admin notifications |

## Rule

This file is not a backlog dump. Keep it to supported or actively owned features.
