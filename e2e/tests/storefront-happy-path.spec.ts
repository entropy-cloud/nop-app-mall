import { test, expect } from './auth.js';

test.describe('Storefront happy-path flow', () => {
  test('address -> cart -> checkout -> pay -> order list', async ({ request }) => {
    const uid = Date.now();

    const addAddressResp = await request.post('/r/LitemallAddress__add', {
      data: {
        name: `E2E-${uid}`,
        phone: '13800138000',
        province: 'Guangdong',
        city: 'Shenzhen',
        county: 'Nanshan',
        addressDetail: `E2E Street ${uid}`,
        areaCode: '440305',
        isDefault: true,
      },
    });
    expect(addAddressResp.status()).toBe(200);
    const addAddressBody = await addAddressResp.json();
    expect(addAddressBody.status).toBe(0);
    const addressId = addAddressBody.data?.id;
    expect(addressId).toBeTruthy();

    const goodsResp = await request.post('/r/LitemallGoods__frontList', {
      data: { page: 1, pageSize: 1 },
    });
    expect(goodsResp.status()).toBe(200);
    const goodsBody = await goodsResp.json();
    expect(goodsBody.status).toBe(0);
    const goods = goodsBody.data?.items?.[0];
    expect(goods).toBeTruthy();
    const goodsId = goods.id;

    const skuResp = await request.post('/r/LitemallGoodsProduct__findList', {
      data: {
        query: {
          offset: 0,
          limit: 1,
          filter: {
            eq: ['goodsId', goodsId],
          },
        },
      },
    });
    expect(skuResp.status()).toBe(200);
    const skuBody = await skuResp.json();
    expect(skuBody.status).toBe(0);
    const product = skuBody.data?.items?.[0];
    expect(product).toBeTruthy();

    const addCartResp = await request.post('/r/LitemallCart__addGoods', {
      data: {
        goodsId,
        productId: product.id,
        number: 1,
      },
    });
    expect(addCartResp.status()).toBe(200);
    const addCartBody = await addCartResp.json();
    expect(addCartBody.status).toBe(0);

    const submitResp = await request.post('/r/LitemallOrder__submit', {
      data: {
        addressId,
        message: 'e2e happy path',
        freightPrice: 0,
      },
    });
    expect(submitResp.status()).toBe(200);
    const submitBody = await submitResp.json();
    expect(submitBody.status).toBe(0);
    const orderId = submitBody.data?.id;
    expect(orderId).toBeTruthy();

    const payResp = await request.post('/r/LitemallOrder__pay', {
      data: { orderId },
    });
    expect(payResp.status()).toBe(200);
    const payBody = await payResp.json();
    expect(payBody.status).toBe(0);
    expect(payBody.data?.orderStatus).toBe(201);

    const myOrdersResp = await request.post('/r/LitemallOrder__myOrders', {
      data: {},
    });
    expect(myOrdersResp.status()).toBe(200);
    const myOrdersBody = await myOrdersResp.json();
    expect(myOrdersBody.status).toBe(0);
    const items = myOrdersBody.data || [];
    const hit = items.find((item: any) => item.id === orderId);
    expect(hit).toBeTruthy();
    expect(hit.orderStatus).toBe(201);
  });
});
