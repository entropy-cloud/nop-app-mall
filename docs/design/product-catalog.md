# Product Catalog Business Design

## Purpose

Describe the product-catalog domain for categories, brands, goods, SKU variants, and catalog search.

## Boundary

- This document owns business concepts, user/admin behavior, and catalog rules.
- Persisted entities, fields, dictionaries, and relationship details are defined in `model/app-mall.orm.xml`.
- Technical implementation strategy belongs in `docs/architecture/`.

## Domain Overview

The catalog follows a three-level business model:

- Category groups goods for navigation and filtering.
- Goods represent the sellable product concept at SPU level.
- SKU represents the purchasable variant selected by the user.

Supporting concepts:

- Brand identifies the commercial brand attached to goods.
- Specification defines selectable variant dimensions such as color or size.
- Attribute defines display-only descriptive properties such as origin or material.

## Business Relationships

- Categories are hierarchical and drive navigation.
- Goods belong to a leaf category and may optionally belong to a brand.
- One goods item can have many SKU variants.
- Specifications describe how SKU variants differ from each other.
- Attributes describe goods but do not affect purchasing.

## Category Management

### Business Rules

- Categories use a two-level browsing structure for storefront navigation.
- Category names must be clear and unique within the same parent.
- Goods can only be assigned to leaf categories.
- A category cannot be removed while it still has child categories or assigned goods.
- Sort order controls display order in admin and storefront navigation.

### Supported Behavior

- Admin users can create, edit, reorder, and remove categories within the supported hierarchy rules.
- Mall users can browse category trees and enter goods lists from category navigation.

## Brand Management

### Business Rules

- Brand identity is optional for goods, but once used it should be presented consistently.
- Brands without active goods may be hidden from storefront-first experiences.
- Brand ordering is business-controlled for storefront presentation.

### Supported Behavior

- Admin users manage brand information.
- Mall users can browse brand-led entry points and filter goods by brand where relevant.

## Goods And SKU Management

### Goods (SPU) Rules

- Goods define the shared commercial identity shown to users.
- Goods must belong to a leaf category before they can be sold.
- Goods must be on sale to appear in storefront search and browsing.
- Goods should have at least one SKU before they become purchasable.
- Rich media, brief descriptions, and detailed descriptions are part of the goods presentation baseline.

### SKU Rules

- Each SKU is one purchasable combination of specification values.
- SKU price and stock are the values that matter at purchase time.
- Stock cannot go below zero.
- Users must not be allowed to purchase unavailable SKU quantity.

### Pricing Semantics

- Reference price is display-oriented.
- Retail price is the normal selling price shown in the catalog.
- SKU price may vary from the default goods-level selling price.
- Goods listing should present a price or price range that matches available SKU data.

### Supported Behavior

- Admin users can create goods, maintain variants, manage sale status, and control merchandising flags such as new or hot.
- Mall users can browse goods lists, open goods detail pages, choose specifications, and purchase SKU variants.

## Specifications And Attributes

### Specifications

- Specifications describe user-selectable dimensions that lead to SKU choice.
- Specification values should map cleanly to actual SKU combinations.
- Visual specifications may include supporting media when needed.

### Attributes

- Attributes are descriptive only.
- Attributes help users understand the goods but do not change SKU selection or pricing.

## Search And Discovery

### Storefront Search

- Search covers goods that are currently on sale.
- Users can filter by category, brand, merchandising flags, and price-related criteria.
- Sorting should support business-relevant options such as price, freshness, and default merchandising order.

### Admin Search

- Admin search must support faster operational lookup across goods regardless of storefront sale status.
- Admin search should expose enough information to manage goods, variants, and sale state efficiently.

## Consistency Rules

- Goods, SKU variants, specifications, and attributes must stay mutually consistent after admin edits.
- Users should see the current sellable price and availability at checkout time, even if catalog data changed after add-to-cart.
- Orders must preserve the purchased goods and SKU meaning even if catalog content changes later.

## Adjacent Scope

- Search history, favorites, and browse footprint are owned by `marketing-and-promotions.md`.
- Promotion pricing such as coupon and groupon effects are owned by `order-and-cart.md` and `marketing-and-promotions.md`.
