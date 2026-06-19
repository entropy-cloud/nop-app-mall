import { test, expect } from './auth.js';

// Explore 结论 (2026-06-19): nop/123 凭证在 %dev profile 可访问全部 31 个 admin 实体 findPage (status:0)。
// 所有 31 个 main.page.yaml 页面均可渲染 (status=0 type=page has_body)。

const ADMIN_ENTITIES = [
  'LitemallAd', 'LitemallAddress', 'LitemallAftersale', 'LitemallBrand',
  'LitemallCart', 'LitemallCategory', 'LitemallCollect', 'LitemallComment',
  'LitemallCoupon', 'LitemallCouponUser', 'LitemallFeedback', 'LitemallFootprint',
  'LitemallGoods', 'LitemallGoodsAttribute', 'LitemallGoodsProduct',
  'LitemallGoodsSpecification', 'LitemallGroupon', 'LitemallGrouponRules',
  'LitemallIssue', 'LitemallKeyword', 'LitemallLog', 'LitemallNotice',
  'LitemallNoticeAdmin', 'LitemallOrder', 'LitemallOrderGoods', 'LitemallRegion',
  'LitemallResetCode', 'LitemallSearchHistory', 'LitemallStorage', 'LitemallSystem',
  'LitemallTopic',
];

const ADMIN_PAGES = ADMIN_ENTITIES.map(e => `${e}/main`);

test.describe('Admin entity RPC smoke', () => {
  for (const entity of ADMIN_ENTITIES) {
    test(`${entity}__findPage responds ok`, async ({ request }) => {
      const resp = await request.post(`/r/${entity}__findPage`, {
        data: { query: { offset: 0, limit: 1 } },
      });
      expect(resp.status(), `POST /r/${entity}__findPage`).toBe(200);
      const body = await resp.json();
      expect(body.status, `${entity} should return status 0`).toBe(0);
      expect(body.data, `${entity} should have data field`).toBeTruthy();
    });
  }
});

test.describe('Admin page rendering smoke', () => {
  for (const pagePath of ADMIN_PAGES) {
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
