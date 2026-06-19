import { test, expect } from './auth.js';
import type { APIRequestContext } from '@playwright/test';

// 售后与退款 e2e 串联。Explore 已确认 nop 凭证可调 admin 操作（batchApprove/refund）。
// 只测已有退款逻辑，不改业务代码（Phase 3 保护区域约束）。
async function createAndPayOrder(request: APIRequestContext): Promise<string> {
  const uid = Date.now();
  const addrResp = await request.post('/r/LitemallAddress__add', {
    data: {
      name: `E2E-AS-${uid}`,
      phone: '13800138000',
      province: 'Guangdong',
      city: 'Shenzhen',
      county: 'Nanshan',
      addressDetail: `Aftersale Street ${uid}`,
      areaCode: '440305',
      isDefault: true,
    },
  });
  const addressId = (await addrResp.json()).data?.id;

  const goodsId = (await (await request.post('/r/LitemallGoods__frontList', {
    data: { page: 1, pageSize: 1 },
  })).json()).data?.items?.[0]?.id;
  expect(goodsId).toBeTruthy();

  const productId = (await (await request.post('/r/LitemallGoodsProduct__findList', {
    data: { query: { offset: 0, limit: 1, filter: { $type: 'eq', name: 'goodsId', value: goodsId } } },
  })).json()).data?.[0]?.id;
  expect(productId).toBeTruthy();

  await request.post('/r/LitemallCart__addGoods', { data: { goodsId, productId, number: 1 } });
  const submitBody = await (await request.post('/r/LitemallOrder__submit', {
    data: { addressId, message: 'aftersale e2e', freightPrice: 0 },
  })).json();
  expect(submitBody.status).toBe(0);
  const orderId = submitBody.data?.id;

  const payBody = await (await request.post('/r/LitemallOrder__pay', { data: { orderId } })).json();
  expect(payBody.data?.orderStatus).toBe(201);
  return orderId;
}

test.describe('Aftersale & refund flow', () => {
  test('apply -> batchApprove -> refund (unshipped PAY order -> 203)', async ({ request }) => {
    const orderId = await createAndPayOrder(request);

    const applyResp = await request.post('/r/LitemallAftersale__apply', {
      data: { orderId, type: 0, reason: 'e2e aftersale test', amount: 1 },
    });
    const applyBody = await applyResp.json();
    expect(applyBody.status).toBe(0);
    const aftersaleId = applyBody.data?.id;
    expect(aftersaleId).toBeTruthy();

    const approveResp = await request.post('/r/LitemallAftersale__batchApprove', {
      data: { ids: [aftersaleId] },
    });
    expect((await approveResp.json()).status).toBe(0);

    const refundResp = await request.post('/r/LitemallAftersale__refund', {
      data: { id: aftersaleId },
    });
    const refundBody = await refundResp.json();
    expect(refundBody.status).toBe(0);

    const orderResp = await request.post('/r/LitemallOrder__getMyOrder', { data: { orderId } });
    const orderBody = await orderResp.json();
    expect(orderBody.status).toBe(0);
    expect(orderBody.data?.orderStatus).toBe(203);
  });

  test('cancel CREATED order (101 -> 102)', async ({ request }) => {
    const uid = Date.now();
    const addrResp = await request.post('/r/LitemallAddress__add', {
      data: {
        name: `E2E-CC-${uid}`,
        phone: '13800138000',
        province: 'Guangdong',
        city: 'Shenzhen',
        county: 'Nanshan',
        addressDetail: `Cancel Street ${uid}`,
        areaCode: '440305',
        isDefault: true,
      },
    });
    const addressId = (await addrResp.json()).data?.id;

    const goodsId = (await (await request.post('/r/LitemallGoods__frontList', {
      data: { page: 1, pageSize: 1 },
    })).json()).data?.items?.[0]?.id;
    const productId = (await (await request.post('/r/LitemallGoodsProduct__findList', {
      data: { query: { offset: 0, limit: 1, filter: { $type: 'eq', name: 'goodsId', value: goodsId } } },
    })).json()).data?.[0]?.id;

    await request.post('/r/LitemallCart__addGoods', { data: { goodsId, productId, number: 1 } });
    const submitBody = await (await request.post('/r/LitemallOrder__submit', {
      data: { addressId, message: 'cancel e2e', freightPrice: 0 },
    })).json();
    const orderId = submitBody.data?.id;

    const cancelResp = await request.post('/r/LitemallOrder__cancel', { data: { orderId } });
    const cancelBody = await cancelResp.json();
    expect(cancelBody.status).toBe(0);
    expect(cancelBody.data?.orderStatus).toBe(102);
  });
});
