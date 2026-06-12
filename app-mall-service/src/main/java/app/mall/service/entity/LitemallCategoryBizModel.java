
package app.mall.service.entity;

import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.FilterBeans;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.api.core.exceptions.NopException;
import io.nop.biz.crud.CrudBizModel;
import io.nop.core.context.IServiceContext;
import app.mall.biz.ILitemallCategoryBiz;
import app.mall.biz.ILitemallGoodsBiz;
import app.mall.dao.entity.LitemallCategory;
import app.mall.dao.entity.LitemallGoods;
import jakarta.inject.Inject;

import java.util.*;
import java.util.stream.Collectors;

import static app.mall.service.AppMallErrors.*;

@BizModel("LitemallCategory")
public class LitemallCategoryBizModel extends CrudBizModel<LitemallCategory> implements ILitemallCategoryBiz {

    @Inject
    ILitemallGoodsBiz goodsBiz;

    public LitemallCategoryBizModel() {
        setEntityName(LitemallCategory.class.getName());
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public List<Map<String, Object>> getCategoryTree() {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCategory.PROP_NAME_deleted, false));
        List<LitemallCategory> all = dao().findAllByQuery(query);

        Map<String, List<LitemallCategory>> grouped = all.stream()
                .collect(Collectors.groupingBy(c -> {
                    String pid = c.getPid();
                    return pid != null ? pid : "0";
                }));

        List<LitemallCategory> roots = grouped.getOrDefault("0", Collections.emptyList());
        List<Map<String, Object>> tree = new ArrayList<>();
        for (LitemallCategory root : roots) {
            tree.add(buildTreeNode(root, grouped));
        }
        return tree;
    }

    private Map<String, Object> buildTreeNode(LitemallCategory category, Map<String, List<LitemallCategory>> grouped) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", category.getId());
        node.put("name", category.getName());
        node.put("iconUrl", category.getIconUrl());
        node.put("picUrl", category.getPicUrl());

        List<LitemallCategory> children = grouped.getOrDefault(category.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<Map<String, Object>> childNodes = new ArrayList<>();
            for (LitemallCategory child : children) {
                childNodes.add(buildTreeNode(child, grouped));
            }
            node.put("children", childNodes);
        }
        return node;
    }

    @Override
    @BizQuery
    public List<LitemallCategory> getCategoryList() {
        QueryBean query = new QueryBean();
        query.addFilter(FilterBeans.eq(LitemallCategory.PROP_NAME_deleted, false));
        return dao().findAllByQuery(query);
    }

    @Override
    protected void defaultPrepareDelete(LitemallCategory entity, IServiceContext context) {
        QueryBean childQuery = new QueryBean();
        childQuery.addFilter(FilterBeans.eq(LitemallCategory.PROP_NAME_pid, entity.getId()));
        childQuery.addFilter(FilterBeans.eq(LitemallCategory.PROP_NAME_deleted, false));
        long childCount = dao().countByQuery(childQuery);
        if (childCount > 0) {
            throw new NopException(ERR_CATEGORY_HAS_CHILDREN)
                    .param("categoryId", entity.getId());
        }

        QueryBean goodsQuery = new QueryBean();
        goodsQuery.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_categoryId, entity.getId()));
        goodsQuery.addFilter(FilterBeans.eq(LitemallGoods.PROP_NAME_deleted, false));
        long goodsCount = goodsBiz.findCount(goodsQuery, context);
        if (goodsCount > 0) {
            throw new NopException(ERR_CATEGORY_HAS_PRODUCTS)
                    .param("categoryId", entity.getId());
        }
    }
}
