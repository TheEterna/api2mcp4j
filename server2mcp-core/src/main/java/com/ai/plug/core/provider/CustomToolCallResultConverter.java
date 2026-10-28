package com.ai.plug.core.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.execution.ToolCallResultConverter;
import org.springframework.ai.util.json.JsonParser;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;


/**
 * @header-start
 * @module CustomToolCallResultConverter
 * @keywords 工具结果转换 ToolCallResultConverter convert 返回类型 字符串序列化 Spring AI
 * @updated 2026-10-28 12:20
 * @purpose 把工具方法返回值序列化为字符串结果，供 Spring AI 工具调用通道使用
 * @layer core
 * @affects 改动影响 Spring AI 通道下所有工具调用的返回值形态
 * @see McpCallToolResultConverter — MCP 通道下的结果转换 SPI（另一条通道，互不影响）
 * @constraint 与 MCP 通道的转换器是两套实现，改一处不会传导到另一处
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/5/24 17:00
 */
@Slf4j
public class CustomToolCallResultConverter implements ToolCallResultConverter {

    @Override
    public String convert(Object result, Type returnType) {
        if (returnType == Void.TYPE) {
            log.debug("The tool has no return type. Converting to conventional response.");
            return JsonParser.toJson("Done");
        }

        // 过滤
        if (result instanceof Mono<?>) {
            result = ((Mono<?>) result).block();
        }

        if (result instanceof RenderedImage) {
            final ByteArrayOutputStream buf = new ByteArrayOutputStream(1024 * 4);
            try {
                ImageIO.write((RenderedImage) result, "PNG", buf);
            }
            catch (IOException e) {
                return "Failed to convert tool result to a base64 image: " + e.getMessage();
            }
            return Base64.getEncoder().encodeToString(buf.toByteArray());
        }
        else {
            log.debug("Converting tool result to JSON.");
            return JsonParser.toJson(result);
        }

    }
}
