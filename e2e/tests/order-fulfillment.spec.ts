import { test, expect } from './auth.js';

// 承接 storefront-happy-path：submit -> pay(201) 之后，续测履约后半段 ship(301) -> confirm(401)。
// Explore 结论：nop/123 凭证在 %dev profile 可调 admin 操作（ship），无需独立 admin 凭证。
test.describe('Order fulfillment: ship -> confirm', () => {
  test('submit -> pay(201) -> ship(301) -> confirm(401)', async ({ request }) => {
    const uid = Date.now();

    const addrResp = await request.post('/r/LitemallAddress__add', {
      data: {
        name: `E2E-FUL-${uid}`,
        phone: '13800138000',
        province: 'Guangdong',
        city: 'Shenzhen',
        county: 'Nanshan',
        addressDetail: `Fulfillment Street ${uid}`,
        areaCode: '440305',
        isDefault: true,
      },
    });
    expect(addrResp.status()).toBe(200);
    const addressId = (await addrResp.json()).data?.id;
    expect(addressId).toBeTruthy();

    const goodsResp = await request.post('/r/LitemallGoods__frontList', {
      data: { page: 1, pageSize: 1 },
    });
    const goodsId = (await goodsResp.json()).data?.items?.[0]?.id;
    expect(goodsId).toBeTruthy();

    const skuResp = await request.post('/r/LitemallGoodsProduct__findList', {
      data: {
        query: {
          offset: 0,
          limit: 1,
          filter: { $type: 'eq', name: 'goodsId', value: goodsId },
        },
      },
    });
    const productId = (await skuResp.json()).data?.[0]?.id;
    expect(productId).toBeTruthy();

    const addCartResp = await request.post('/r/LitemallCart__addGoods', {
      data: { goodsId, productId, number: 1 },
    });
    expect((await addCartResp.json()).status).toBe(0);

    const submitResp = await request.post('/r/LitemallOrder__submit', {
      data: { addressId, message: 'fulfillment e2e', freightPrice: 0 },
    });
    const submitBody = await submitResp.json();
    expect(submitBody.status).toBe(0);
    const orderId = submitBody.data?.id;
    expect(orderId).toBeTruthy();

    const payResp = await request.post('/r/LitemallOrder__pay', { data: { orderId } });
    const payBody = await payResp.json();
    expect(payBody.status).toBe(0);
    expect(payBody.data?.orderStatus).toBe(201);

    const shipResp = await request.post('/r/LitemallOrder__ship', {
      data: { orderId, shipSn: `SF${uid}`, shipChannel: 'SF-Express' },
    });
    const shipBody = await shipResp.json();
    expect(shipBody.status).toBe(0);
    expect(shipBody.data?.orderStatus).toBe(301);

    const confirmResp = await request.post('/r/LitemallOrder__confirm', { data: { orderId } });
    const confirmBody = await confirmResp.json();
    expect(confirmBody.status).toBe(0);
    expect(confirmBody.data?.orderStatus).toBe(401);
  });
});
