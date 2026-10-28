package com.ai.plug.core.spec;

import com.ai.plug.core.utils.GenSchemaUtils;
import tools.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @header-start
 * @module ElicitationTests
 * @keywords 询问 elicitation Schema生成 MCP_SCHEMA_GENERATOR String类型 schema 调试打印
 * @updated 2026-10-28 12:20
 * @purpose 打印 String 类型的 MCP Schema 生成结果，辅助核对 elicitation 表单结构
 * @layer test
 * @author han
 * @header-end
 */

/**
 * @author han
 * @time 2025/7/8 1:24
 */

public class ElicitationTests {

    @Test
    public void testElicitation() {
        // 测试String类型的schema生成
        ObjectNode stringSchema = GenSchemaUtils.MCP_SCHEMA_GENERATOR.generateSchema(String.class);
        System.out.println("String Schema: " + stringSchema.toPrettyString());
        
        Map<String, Object> stringSchemaMap = GenSchemaUtils.objectNodeToMap(stringSchema);
        System.out.println("String Schema Map: " + stringSchemaMap);
    }

}
