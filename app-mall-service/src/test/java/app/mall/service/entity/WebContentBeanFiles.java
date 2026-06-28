package app.mall.service.entity;

import io.nop.api.core.beans.WebContentBean;

import java.io.File;
import java.util.Map;

/**
 * 测试辅助：从 GraphQL RPC 返回的导出结果中提取底层 File。
 * IGraphQLEngine 的 executeRpc 可能返回 WebContentBean 原始对象或其 Map 序列化形式，两者皆支持。
 */
final class WebContentBeanFiles {
    private WebContentBeanFiles() {
    }

    static File contentFile(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof WebContentBean) {
            Object content = ((WebContentBean) data).getContent();
            return toFile(content);
        }
        if (data instanceof Map) {
            Object content = ((Map<?, ?>) data).get("content");
            return toFile(content);
        }
        return null;
    }

    private static File toFile(Object content) {
        if (content instanceof File) {
            return (File) content;
        }
        if (content instanceof java.nio.file.Path) {
            return ((java.nio.file.Path) content).toFile();
        }
        return null;
    }
}
