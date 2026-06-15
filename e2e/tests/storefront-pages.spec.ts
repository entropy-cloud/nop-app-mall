import { test, expect } from './auth.js';

const STOREFRONT_ENTITIES = [
  'LitemallGoods',
  'LitemallCategory',
  'LitemallBrand',
  'LitemallAd',
  'LitemallTopic',
  'LitemallCart',
  'LitemallOrder',
  'LitemallAddress',
  'LitemallCollect',
  'LitemallCoupon',
  'LitemallNotice',
];

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

test.describe('Storefront data RPC smoke', () => {
  for (const entity of STOREFRONT_ENTITIES) {
    test(`${entity}__findPage responds ok`, async ({ request }) => {
      const resp = await request.post(`/r/${entity}__findPage`, {
        data: { query: { offset: 0, limit: 1 } },
      });
      expect(resp.status(), `POST /r/${entity}__findPage`).toBe(200);
      const body = await resp.json();
      expect(body.status, `${entity} should return status 0`).toBe(0);
      expect(Array.isArray(body.data?.items), `${entity} should return items array`).toBe(true);
    });
  }
});

test.describe('Storefront page rendering', () => {
  for (const pagePath of STOREFRONT_PAGES) {
    test(`${pagePath} page renders`, async ({ request }) => {
      const resp = await request.get(`/r/PageProvider__getPage`, {
        params: { path: `/app/mall/pages/${pagePath}.page.yaml` },
      });
      expect(resp.status(), `GET PageProvider__getPage ${pagePath}`).toBe(200);
      const body = await resp.json();
      expect(body.status, `${pagePath} should return status 0`).toBe(0);
      expect(body.data?.type, `${pagePath} should be a page`).toBe('page');
      expect(body.data?.body, `${pagePath} should have body`).toBeTruthy();
    });
  }
});
