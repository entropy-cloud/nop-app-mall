package app.mall.service.entity;

import app.mall.biz.ILitemallCartBiz;
import app.mall.biz.ILitemallGoodsProductBiz;
import app.mall.dao.entity.LitemallCart;
import app.mall.dao.entity.LitemallGoods;
import app.mall.dao.entity.LitemallGoodsProduct;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizMutation;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.core.Name;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import jakarta.inject.Inject;

import java.util.List;

import static app.mall.service.AppMallErrors.ERR_CART_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_CART_PRODUCT_NOT_FOUND;
import static app.mall.service.AppMallErrors.ERR_CART_STOCK_INSUFFICIENT;

@BizModel("LitemallCart")
public class LitemallCartBizModel extends CrudBizModel<LitemallCart> implements ILitemallCartBiz {

    @Inject
    ILitemallGoodsProductBiz goodsProductBiz;

    public LitemallCartBizModel() {
        setEntityName(LitemallCart.class.getName());
    }

    @Override
    @BizMutation
    public LitemallCart addGoods(@Name("goodsId") String goodsId,
                                  @Name("productId") String productId,
                                  @Name("number") int number,
                                  IServiceContext context) {
        String userId = context != null ? context.getUserId() : io.nop.api.core.context.ContextProvider.getOrCreateContext().getUserId();

        LitemallGoodsProduct product = goodsProductBiz.get(productId, false, context);
        if (product == null) {
            throw new NopException(ERR_CART_PRODUCT_NOT_FOUND)
                    .param("productId", productId);
        }

        if (product.getNumber() == null || product.getNumber() < number) {
            throw new NopException(ERR_CART_STOCK_INSUFFICIENT)
                    .param("productId", productId)
                    .param("stock", product.getNumber());
        }

        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_productId, productId));
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_deleted, false));
        List<LitemallCart> existing = findList(query, null, context);

        LitemallCart cart;
        if (existing != null && !existing.isEmpty()) {
            cart = existing.get(0);
            int newNumber = (cart.getNumber() != null ? cart.getNumber() : 0) + number;
            if (product.getNumber() != null && newNumber > product.getNumber()) {
                throw new NopException(ERR_CART_STOCK_INSUFFICIENT)
                        .param("productId", productId)
                        .param("stock", product.getNumber());
            }
            cart.setNumber(newNumber);
            cart.setPrice(product.getPrice());
            updateEntity(cart, "addGoods", context);
        } else {
            cart = newEntity();
            cart.setUserId(userId);
            cart.setGoodsId(goodsId);
            cart.setProductId(productId);
            cart.setNumber(number);
            cart.setChecked(true);
            cart.setPrice(product.getPrice());

            LitemallGoods goods = product.getGoods();
            cart.setGoodsSn(goods != null ? goods.getGoodsSn() : null);
            cart.setGoodsName(goods != null ? goods.getName() : null);
            if (product.getUrl() != null) {
                cart.getPicUrlComponent().copyFrom(product.getUrlComponent());
            } else if (goods != null && goods.getPicUrl() != null) {
                cart.getPicUrlComponent().copyFrom(goods.getPicUrlComponent());
            }
            cart.setSpecifications(product.getSpecifications());

            saveEntity(cart, "addGoods", context);
        }

        return cart;
    }

    @Override
    @BizMutation
    public LitemallCart updateQuantity(@Name("id") String id,
                                        @Name("number") int number,
                                        IServiceContext context) {
        LitemallCart cart = get(id, false, context);
        if (cart == null || Boolean.TRUE.equals(cart.getDeleted())) {
            throw new NopException(ERR_CART_NOT_FOUND)
                    .param("id", id);
        }

        LitemallGoodsProduct product = goodsProductBiz.get(cart.getProductId(), false, context);
        if (product != null && product.getNumber() != null && number > product.getNumber()) {
            throw new NopException(ERR_CART_STOCK_INSUFFICIENT)
                    .param("productId", cart.getProductId())
                    .param("stock", product.getNumber());
        }

        cart.setNumber(number);
        updateEntity(cart, "updateQuantity", context);
        return cart;
    }

    @Override
    @BizMutation
    public LitemallCart check(@Name("id") String id, IServiceContext context) {
        LitemallCart cart = get(id, false, context);
        if (cart == null || Boolean.TRUE.equals(cart.getDeleted())) {
            throw new NopException(ERR_CART_NOT_FOUND)
                    .param("id", id);
        }
        cart.setChecked(true);
        updateEntity(cart, "check", context);
        return cart;
    }

    @Override
    @BizMutation
    public LitemallCart uncheck(@Name("id") String id, IServiceContext context) {
        LitemallCart cart = get(id, false, context);
        if (cart == null || Boolean.TRUE.equals(cart.getDeleted())) {
            throw new NopException(ERR_CART_NOT_FOUND)
                    .param("id", id);
        }
        cart.setChecked(false);
        updateEntity(cart, "uncheck", context);
        return cart;
    }

    @Override
    @BizMutation
    public void checkAll(IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_deleted, false));
        List<LitemallCart> list = findList(query, null, context);
        for (LitemallCart cart : list) {
            cart.setChecked(true);
        }
    }

    @Override
    @BizMutation
    public void uncheckAll(IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_deleted, false));
        List<LitemallCart> list = findList(query, null, context);
        for (LitemallCart cart : list) {
            cart.setChecked(false);
        }
    }

    @Override
    @BizMutation
    public void deleteCart(@Name("id") String id, IServiceContext context) {
        LitemallCart cart = get(id, false, context);
        if (cart == null || Boolean.TRUE.equals(cart.getDeleted())) {
            throw new NopException(ERR_CART_NOT_FOUND)
                    .param("id", id);
        }
        delete(id, context);
    }

    @Override
    @BizMutation
    public void clear(IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_deleted, false));
        List<LitemallCart> list = findList(query, null, context);
        for (LitemallCart cart : list) {
            delete(cart.orm_idString(), context);
        }
    }

    @Override
    @BizQuery
    public List<LitemallCart> checkedList(IServiceContext context) {
        String userId = context.getUserId();
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_userId, userId));
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_checked, true));
        query.addFilter(FilterBeans.eq(LitemallCart.PROP_NAME_deleted, false));
        return findList(query, null, context);
    }
}
