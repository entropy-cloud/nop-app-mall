package app.mall.service.entity;

import app.mall.biz.ILitemallRegionBiz;
import app.mall.dao.entity.LitemallRegion;
import io.nop.api.core.annotations.biz.BizModel;
import io.nop.api.core.annotations.biz.BizQuery;
import io.nop.api.core.annotations.directive.Auth;
import io.nop.biz.crud.CrudBizModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@BizModel("LitemallRegion")
public class LitemallRegionBizModel extends CrudBizModel<LitemallRegion> implements ILitemallRegionBiz {

    public LitemallRegionBizModel() {
        setEntityName(LitemallRegion.class.getName());
    }

    @Override
    @BizQuery
    @Auth(publicAccess = true)
    public List<Map<String, Object>> getRegionTree() {
        List<LitemallRegion> all = dao().findAll();

        Map<String, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (LitemallRegion region : all) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", region.getId());
            node.put("name", region.getName());
            node.put("type", region.getType());
            node.put("code", region.getCode());
            nodeMap.put(region.getId(), node);
        }

        List<Map<String, Object>> provinces = new ArrayList<>();
        for (LitemallRegion region : all) {
            String pid = region.getPid();
            Map<String, Object> node = nodeMap.get(region.getId());
            if (pid == null || "0".equals(pid)) {
                provinces.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(pid);
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children = (List<Map<String, Object>>) parent.get("children");
                    if (children == null) {
                        children = new ArrayList<>();
                        parent.put("children", children);
                    }
                    children.add(node);
                }
            }
        }

        return provinces;
    }
}
