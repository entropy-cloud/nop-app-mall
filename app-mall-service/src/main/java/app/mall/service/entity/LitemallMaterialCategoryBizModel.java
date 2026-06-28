
package app.mall.service.entity;

import app.mall.biz.ILitemallMaterialCategoryBiz;
import app.mall.dao.entity.LitemallMaterialCategory;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.api.core.beans.query.QueryBean;
import io.nop.biz.crud.CrudBizModel;
import io.nop.commons.util.StringHelper;
import io.nop.core.context.IServiceContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@BizModel("LitemallMaterialCategory")
public class LitemallMaterialCategoryBizModel extends CrudBizModel<LitemallMaterialCategory>
        implements ILitemallMaterialCategoryBiz {

    public LitemallMaterialCategoryBizModel() {
        setEntityName(LitemallMaterialCategory.class.getName());
    }

    // 全量查询后在内存组装为树：根节点列表，每个节点含 children，所有层级按 sortOrder 升序。
    // 分类数据量小，内存组装方案（参照 LitemallCategoryBizModel.getCategoryTree 先例）。
    @Override
    @BizQuery
    @Auth(roles = "admin")
    public List<Map<String, Object>> getCategoryTree(IServiceContext context) {
        QueryBean query = new QueryBean();
        List<LitemallMaterialCategory> all = findList(query, null, context);

        Map<String, List<LitemallMaterialCategory>> grouped = all.stream()
                .collect(Collectors.groupingBy(c -> {
                    String pid = c.getParentId();
                    return StringHelper.isEmpty(pid) ? ROOT_KEY : pid;
                }));

        List<LitemallMaterialCategory> roots = grouped.getOrDefault(ROOT_KEY, Collections.emptyList());
        List<LitemallMaterialCategory> sortedRoots = sortBySortOrder(roots);
        List<Map<String, Object>> tree = new ArrayList<>();
        for (LitemallMaterialCategory root : sortedRoots) {
            tree.add(buildTreeNode(root, grouped));
        }
        return tree;
    }

    private Map<String, Object> buildTreeNode(LitemallMaterialCategory category,
                                              Map<String, List<LitemallMaterialCategory>> grouped) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", category.getId());
        node.put("name", category.getName());
        node.put("parentId", category.getParentId());
        node.put("sortOrder", category.getSortOrder());

        List<LitemallMaterialCategory> children = grouped.getOrDefault(category.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<Map<String, Object>> childNodes = new ArrayList<>();
            for (LitemallMaterialCategory child : sortBySortOrder(children)) {
                childNodes.add(buildTreeNode(child, grouped));
            }
            node.put("children", childNodes);
        }
        return node;
    }

    private List<LitemallMaterialCategory> sortBySortOrder(List<LitemallMaterialCategory> list) {
        List<LitemallMaterialCategory> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparingInt(
                (LitemallMaterialCategory c) -> c.getSortOrder() == null ? 0 : c.getSortOrder()));
        return copy;
    }

    private static final String ROOT_KEY = "__root__";
}
