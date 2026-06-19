import { test, expect } from './auth.js';
import type { APIRequestContext } from '@playwright/test';

// 营销流程 e2e：优惠券领用→下单抵扣 + 拼团开团。
// Explore 已确认 nop 凭证可调 admin 操作（LitemallCoupon__save / LitemallGrouponRules__save / publishRules）。
// 拼团参团（joinGroupon）需第二用户（不能加入自己的团），e2e 仅有 nop 单用户 → 参团移入 Deferred。

const iso = (offsetDays: number) => new Date(Date.now() + offsetDays * 86400000).toISOString().slice(0, 19);

async function pickGoodsAndSku(request: APIRequestContext) {
  const goodsId = (await (await request.post('/r/LitemallGoods__frontList', {
    data: { page: 1, pageSize: 1 },
  })).json()).data?.items?.[0]?.id;
  const productId = (await (await request.post('/r/LitemallGoodsProduct__findList', {
    data: { query: { offset: 0, limit: 1, filter: { $type: 'eq', name: 'goodsId', value: goodsId } } },
  })).json()).data?.[0]?.id;
  const goodsName = (await (await request.post('/r/LitemallGoods__frontList', {
    data: { page: 1, pageSize: 1 },
  })).json()).data?.items?.[0]?.name;
  return { goodsId, productId, goodsName };
}

async function addAddressAndCart(request: APIRequestContext, goodsId: string, productId: string) {
  const uid = Date.now();
  const addrResp = await request.post('/r/LitemallAddress__add', {
    data: {
      name: `E2E-MKT-${uid}`, phone: '13800138000', province: 'Guangdong',
      city: 'Shenzhen', county: 'Nanshan', addressDetail: `MKT Street ${uid}`,
      areaCode: '440305', isDefault: true,
    },
  });
  const addressId = (await addrResp.json()).data?.id;
  await request.post('/r/LitemallCart__addGoods', { data: { goodsId, productId, number: 1 } });
  return addressId;
}

test.describe('Marketing flow: coupon + groupon', () => {
  test('coupon: save -> claimCoupon -> submit with couponUserId (couponPrice > 0)', async ({ request }) => {
    const couponSaveResp = await request.post('/r/LitemallCoupon__save', {
      data: {
        data: {
          name: 'E2E-COUPON', tag: 'e2e', total: 100, discount: 1, min: 0, limit: 1,
          type: 0, status: 0, goodsType: 0, goodsValue: '', code: `E2E-${Date.now()}`,
          timeType: 0, days: 30, startTime: iso(-1), endTime: iso(30),
        },
      },
    });
    const couponSaveBody = await couponSaveResp.json();
    expect(couponSaveBody.status).toBe(0);
    const couponId = couponSaveBody.data?.id;
    expect(couponId).toBeTruthy();

    const claimResp = await request.post('/r/LitemallCouponUser__claimCoupon', {
      data: { couponId },
    });
    const claimBody = await claimResp.json();
    expect(claimBody.status).toBe(0);
    const couponUserId = claimBody.data?.id;
    expect(couponUserId).toBeTruthy();

    const { goodsId, productId } = await pickGoodsAndSku(request);
    const addressId = await addAddressAndCart(request, goodsId, productId);

    const submitResp = await request.post('/r/LitemallOrder__submit', {
      data: { addressId, message: 'coupon e2e', freightPrice: 0, couponUserId },
    });
    const submitBody = await submitResp.json();
    expect(submitBody.status).toBe(0);
    expect(Number(submitBody.data?.couponPrice)).toBeGreaterThan(0);
  });

  test('groupon: save rules -> publishRules -> openGroupon (status=1)', async ({ request }) => {
    const { goodsId, goodsName } = await pickGoodsAndSku(request);

    const rulesSaveResp = await request.post('/r/LitemallGrouponRules__save', {
      data: {
        data: {
          goodsId, goodsName: goodsName || 'e2e-groupon-goods',
          discount: 1, discountMember: 2, expireTime: iso(7), status: 2,
        },
      },
    });
    const rulesSaveBody = await rulesSaveResp.json();
    expect(rulesSaveBody.status).toBe(0);
    const rulesId = rulesSaveBody.data?.id;
    expect(rulesId).toBeTruthy();

    const publishResp = await request.post('/r/LitemallGrouponRules__publishRules', {
      data: { id: rulesId },
    });
    expect((await publishResp.json()).status).toBe(0);

    const { productId } = await pickGoodsAndSku(request);
    const addressId = await addAddressAndCart(request, goodsId, productId);

    const submitResp = await request.post('/r/LitemallOrder__submit', {
      data: { addressId, message: 'groupon e2e', freightPrice: 0 },
    });
    const submitBody = await submitResp.json();
    expect(submitBody.status).toBe(0);
    const orderId = submitBody.data?.id;

    const openResp = await request.post('/r/LitemallGroupon__openGroupon', {
      data: { rulesId, orderId },
    });
    const openBody = await openResp.json();
    expect(openBody.status).toBe(0);
    expect(openBody.data?.status).toBe(1);
  });
});
