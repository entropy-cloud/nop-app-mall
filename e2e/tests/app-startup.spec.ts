import { test, expect } from './auth.js';

test.describe('App startup smoke', () => {
  test('root page loads', async ({ page }) => {
    const resp = await page.goto('/');
    expect(resp?.status()).toBe(200);
  });

  test('GraphQL endpoint responds', async ({ request }) => {
    const resp = await request.post('/graphql', {
      data: {
        query: '{ LitemallBrand__findPage(query:{offset:0,limit:1}){ total } }',
      },
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.data).toBeTruthy();
    expect(body.errors).toBeUndefined();
  });
});
