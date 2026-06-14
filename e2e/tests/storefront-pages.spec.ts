import { test, expect } from '@playwright/test';

const STOREFRONT_PAGES = [
  'mall/home/home',
  'mall/category/category',
  'mall/goods/goods-detail',
  'mall/cart/cart',
  'mall/checkout/checkout',
  'mall/checkout/order-result',
  'mall/user/login',
  'mall/user/user-center',
  'mall/user/order-list',
  'mall/user/order-detail',
  'mall/user/address',
];

test.describe('Storefront page provider', () => {
  for (const pagePath of STOREFRONT_PAGES) {
    test(`${pagePath} returns valid page JSON`, async ({ request }) => {
      const resp = await request.get(`/p/${pagePath}`);
      expect(resp.status(), `GET /p/${pagePath}`).toBe(200);
      const body = await resp.json();
      expect(body).toBeTruthy();
      expect(typeof body).toBe('object');
    });
  }
});
