import { test, expect } from './auth.js';

// P36 goods-ops workbench page render smoke
const GOODS_OPS_PAGES = [
  'mall/goods-ops/goods-batch',
  'mall/goods-ops/goods-io',
  'mall/goods-ops/stock-warning',
  'mall/goods-ops/comment-review',
];

test.describe('P36 goods-ops page rendering smoke', () => {
  for (const pagePath of GOODS_OPS_PAGES) {
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
