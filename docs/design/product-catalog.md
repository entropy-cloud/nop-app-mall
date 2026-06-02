# Product Catalog Business Design

## Purpose

Describe the product catalog domain model, business rules, and workflows for goods, categories, brands, specifications, and SKUs.

## Domain Overview

The product catalog is the foundation of the mall system. It follows a three-layer model: **Category → Goods (SPU) → Product (SKU)**, inspired by industry-standard e-commerce patterns from macrozheng/mall and linlinjava/litemall.

### Core Entities

| Entity | Table | Role |
|--------|-------|------|
| Category | `app_mall_category` | Hierarchical product classification (tree structure) |
| Brand | `app_mall_brand` | Brand information associated with goods |
| Goods | `app_mall_goods` | SPU-level product information (shared properties) |
| GoodsProduct | `app_mall_goods_product` | SKU-level purchasable variant (price, stock) |
| GoodsSpecification | `app_mall_goods_specification` | Per-goods specification definitions |
| GoodsAttribute | `app_mall_goods_attribute` | Per-goods display parameters |

### Entity Relationships

```
Category (tree, pid self-reference)
  └── Goods (SPU, belongs to category and optional brand)
        ├── GoodsProduct (SKU, purchasable variant with price+stock)
        ├── GoodsSpecification (spec definitions like color/size)
        └── GoodsAttribute (display params like origin/material)
Brand
  └── Goods (optional brand association)
```

## Category Management

### Data Model

- Tree structure using `pid` (parent ID) for self-referencing hierarchy
- Two-level hierarchy: L1 (top-level) → L2 (sub-category)
- Fields: name, keywords (JSON), icon_url, pic_url, level, sort_order, desc (category slogan/ad text)
- `pid = 0` denotes top-level category

### Business Rules

- Category name is required and unique within the same parent
- Category level is limited to 2 levels (L1, L2)
- Products can only be assigned to L2 (leaf) categories
- Deleting a category requires reassigning or removing its products first
- Sort order determines display sequence

### Admin Operations

- **Create category**: set name, pid, icon, pic, keywords, sort order
- **Update category**: modify all fields; cannot change to create circular references
- **Delete category**: only if no goods assigned and no child categories
- **Reorder**: update sort_order values
- **List**: tree structure with nested children

### User Operations

- **Browse categories**: L1 list with L2 children; flat list of L2 categories
- **Filter by category**: goods list filtered by category ID (includes sub-categories)

## Brand Management

### Data Model

- Fields: name, description, pic_url, sort_order
- Optional: floor_price (lowest price among associated goods, computed)

### Business Rules

- Brand name is required
- Brand pic is recommended but not required
- Floor price is computed from associated goods, not manually set
- Brand without associated goods may be hidden from user browsing

### Admin Operations

- **Create/update/delete brand**: standard CRUD
- **List**: paginated with optional name filtering

### User Operations

- **Browse brands**: list brands with pic and floor price
- **Filter by brand**: goods list filtered by brand ID

## Goods (SPU) Management

### Data Model

- Core fields: goods_sn (unique code), name, category_id, brand_id, is_on_sale, retail_price, counter_price
- Rich content: brief, gallery (JSON array), detail (rich text)
- Flags: is_new, is_hot, is_on_sale
- Sorting: sort_order
- Unit: unit (default "件")
- Share: share_url (for groupon sharing images, deferred)

### Business Rules

- Goods name is required (1-127 characters)
- Goods code (goods_sn) must be unique
- Category is required (must be L2 leaf category)
- Retail price must be > 0
- At least one SKU (GoodsProduct) must exist for goods to be on sale
- Goods must be "on sale" (is_on_sale=1) to be visible to users
- Default state: on sale when created
- Gallery stored as JSON array of image URLs
- Detail stored as rich text (HTML)

### Price Model

```
counter_price (专柜价/原价)  — display-only reference price
retail_price (零售价)        — actual selling price at SPU level
SKU price                    — per-variant price (may differ from retail_price)
```

When displaying goods in list: show retail_price range (min ~ max of all SKUs).
When displaying goods detail: show each SKU's specific price.

### Admin Operations

- **Create goods**: provide name, sn, category, brand, specs, SKU list, images, detail
- **Update goods**: modify all fields including adding/removing SKUs
- **On/Off sale**: toggle is_on_sale flag
- **Delete goods**: soft delete (set deleted=1); not allowed if active orders exist
- **List**: paginated with filters (category, brand, keyword, sale status, new/hot flag)
- **Batch update**: on/off sale, set new/hot flag

### User Operations

- **Browse goods list**: paginated, filter by category/brand/keyword/price range, sort by price/sales/time
- **View goods detail**: full info with specs, SKUs, attributes, images, related comments
- **New arrivals**: goods with is_new=1, sorted by creation time
- **Hot goods**: goods with is_hot=1, sorted by sort_order

## Goods Specification

### Data Model

- Fields: goods_id, name (spec name like "Color"), value (spec value like "Red"), pic_url
- Multiple specifications per goods (e.g., "Color" and "Size")

### Business Rules

- Each goods can have multiple specifications
- Specification name groups related values (e.g., "Color" → Red, Blue, Green)
- Specification values map to SKU combinations
- Spec pic_url is optional (used for visual specs like colors)

## Goods Product (SKU)

### Data Model

- Fields: goods_id, specifications (JSON array), price, stock (number), url (variant image)
- specifications example: `["Red", "XL"]` — corresponds to GoodsSpecification values in order

### Business Rules

- Each unique combination of specification values creates one SKU
- SKU price can differ from goods retail_price
- Stock (number) must be >= 0
- Stock cannot go negative during purchase (checked at order time, decremented in transaction)
- A goods must have at least one SKU to be purchasable
- When displaying goods, show all SKUs with price and stock

### Stock Management

- **Decrease stock**: when order is placed (within order transaction)
- **Increase stock**: when order is cancelled, refunded, or after-sale approved
- **Stock check**: must verify stock > 0 before allowing add-to-cart and order placement
- Stock operations are part of order transaction to ensure consistency

## Goods Attribute

### Data Model

- Fields: goods_id, name (attribute name like "Origin"), value (attribute value like "China")

### Business Rules

- Display-only parameters shown in goods detail page
- No impact on purchasing or SKU selection
- Examples: origin, material, weight, dimensions, warranty

## Search and Filtering

### User-Facing Search

- Keyword search across goods name, goods_sn, and keywords field
- Only search goods with is_on_sale=1
- Support filters: category, brand, price range, is_new, is_hot
- Sort options: price (asc/desc), creation time (desc default), sort_order
- Paginated results with goods ID, name, retail_price, pic, is_new, is_hot

### Admin Search

- Full-text search across all goods regardless of sale status
- Additional filters: sale status, category, brand, new/hot flag
- Include stock information in results

## Data Consistency Rules

### Goods Creation Transaction

1. Create goods record
2. Create all specification records
3. Create all SKU (goods_product) records
4. Create all attribute records
5. All must succeed or all roll back

### Stock Operations

- Stock decrease: part of order creation transaction
- Stock increase: part of order cancel/refund transaction
- Stock must never go negative (application-level check before decrement)
- Concurrent stock access: optimistic locking via Nop ORM version field or database-level row lock

## Nop Platform Implementation Notes

- Entities defined in `model/app-mall.orm.xml`
- Generated code in `app-mall-dao/`
- Business logic in `app-mall-service/` BizModel classes extending CrudBizModel
- Admin CRUD pages in `app-mall-web/` AMIS `.view.xml` files
- Do not manually edit generated code; regenerate from XML models
- Use `@BizQuery` for read operations, `@BizMutation` for write operations
- Use `@BizMutation` annotation auto-wraps transactions; do not add `@Transactional`

## Reference Projects Comparison

| Aspect | mall (macrozheng) | litemall | nop-app-mall |
|--------|-------------------|----------|-------------|
| Attribute model | Shared attribute definitions across categories | Per-goods inline specifications | Per-goods inline (litemall style) |
| SKU spec storage | sp_0..sp_3 columns + JSON attr | JSON array | JSON array (litemall style) |
| Scale target | Enterprise (bigint) | Small-medium (int) | Demo (int) |
| Module separation | By deployment target | By layer | By Nop module convention |
