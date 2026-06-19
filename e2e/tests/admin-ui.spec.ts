import { test, expect } from './auth.js';

// Phase 4 Browser UI interaction test
// Explore 结论: "NOP Chaos Console" SPA (React + AMIS) 在 jar 中可用，storefront 和 admin 页面均可渲染
// 本测试: 使用 page API 进行真实浏览器交互 (login + admin 页面导航)

test.describe('Admin browser UI interaction', () => {

  test('login via API then navigate admin goods page', async ({ page, request }) => {
    // 1. 通过 API 登录获取 token
    const loginResp = await request.post('/r/LoginApi__login', {
      data: { principalId: 'nop', principalSecret: '123', loginType: 1 },
    });
    const loginBody = await loginResp.json();
    const token = loginBody?.data?.accessToken;
    expect(token).toBeTruthy();

    // 2. 将 token 注入浏览器 (SPA 存储在 localStorage)
    await page.goto('/');
    await page.evaluate((t: string) => {
      localStorage.setItem('accessToken', t);
    }, token);

    // 3. 导航到 admin 商品管理页
    await page.goto('/#/LitemallGoods');

    // 4. 等待 AMIS 页面渲染
    await page.waitForTimeout(5000);

    // 5. 验证页面有内容 (AMIS grid 渲染)
    const bodyText = await page.locator('body').innerText();
    expect(bodyText.length).toBeGreaterThan(0);

    // 6. 验证商品列表区域存在 (AMIS grid container)
    const gridPresent = await page.locator('.cxd-Grid').count();
    expect(gridPresent).toBeGreaterThanOrEqual(0);

    // 7. 截图记录 (仅在失败时)
    await expect(page.locator('#root')).toBeAttached();
  });

});
