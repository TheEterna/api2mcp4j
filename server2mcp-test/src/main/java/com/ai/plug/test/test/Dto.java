package com.ai.plug.test.test;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @header-start
 * @module Dto
 * @keywords 数据载体 DTO POJO @ToolParam描述 @Schema注解 Swagger标题 Schema生成
 *           工具参数示例 lombok
 * @updated 2026-10-28 12:20
 * @purpose 演示用的数据载体，承载工具参数描述与 Schema 注解的生成效果
 * @layer test
 * @author han
 * @header-end
 */

/**
 * @author 韩
 */
@Data
@Schema(description = "我是对象 666")
public class Dto {
    @ToolParam(description = "这个是id")
    private Integer id;
    @Schema(title = "666", description = "牛")
    private String score;

    public Dto(Integer id, String score) {
        this.id = id;
        this.score = score;
    }

    public Dto() {
    }
}