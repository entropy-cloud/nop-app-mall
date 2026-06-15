import { test as base, expect, request as standaloneRequest } from '@playwright/test';
import type { APIRequestContext } from '@playwright/test';

const userName = process.env.E2E_USER || 'nop';
const password = process.env.E2E_PASSWORD || '123';

async function login(baseURL: string): Promise<string> {
  const ctx = await standaloneRequest.newContext({ baseURL });
  try {
    const resp = await ctx.post('/r/LoginApi__login', {
      data: { principalId: userName, principalSecret: password, loginType: 1 },
    });
    const body = await resp.json();
    const token = body?.data?.accessToken;
    if (!token) throw new Error(`Login failed for user "${userName}": ${JSON.stringify(body)}`);
    return token;
  } finally {
    await ctx.dispose();
  }
}

export const test = base.extend<{ request: APIRequestContext }>({
  request: async ({ baseURL }, use) => {
    const token = await login(baseURL!);
    const authed = await standaloneRequest.newContext({
      baseURL,
      extraHTTPHeaders: { Authorization: `Bearer ${token}` },
    });
    await use(authed);
    await authed.dispose();
  },
});

export { expect };
