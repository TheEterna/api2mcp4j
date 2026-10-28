package com.ai.plug.core.parser.tool.param;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiParam;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @header-start
 * @module Swagger2ParamParser
 * @keywords 参数解析器 责任链 Swagger2 ApiParam ApiImplicitParam ApiImplicitParams 参数必填
 *           参数描述 AbstractParamParser 方法级参数声明
 * @updated 2026-10-28 12:20
 * @purpose 从 Swagger2 参数注解（含方法级声明）提取参数必填标记与描述
 * @layer core
 * @author han
 * @header-end
 */

/**
 * @author 韩
 * time: 2025/4/30 2:56
 */
public class Swagger2ParamParser extends AbstractParamParser {

    @Override
    public Boolean doParamRequiredParse(Method method, Class<?> toolClass, int index) {
        Parameter parameter = method.getParameters()[index];

        ApiParam apiParamAnnotation = parameter.getAnnotation(ApiParam.class);
        if (apiParamAnnotation != null) {
            return apiParamAnnotation.required();
        }
        ApiImplicitParam apiImplicitParamAnnotation = method.getAnnotation(ApiImplicitParam.class);
        if (apiImplicitParamAnnotation != null && apiImplicitParamAnnotation.name().equals(parameter.getName())) {

            return apiImplicitParamAnnotation.required();
        }
        ApiImplicitParams apiImplicitParamsAnnotation = method.getAnnotation(ApiImplicitParams.class);
        if (apiImplicitParamsAnnotation != null) {
            for (ApiImplicitParam itemApiImplicitParam : apiImplicitParamsAnnotation.value()) {
                // 如果在parameters里有 这个属性就返回
                if (itemApiImplicitParam.name().equals(parameter.getName())) {
                    return itemApiImplicitParam.required();
                }
            }
        }
        return null;
    }

    @Override
    public String doParamDesParse(Method method, Class<?> toolClass, int index) {
        Parameter parameter = method.getParameters()[index];

        ApiParam apiParamAnnotation = parameter.getAnnotation(ApiParam.class);
        if (apiParamAnnotation != null && StringUtils.hasText(apiParamAnnotation.value())) {
            return apiParamAnnotation.value();
        }

        ApiImplicitParam apiImplicitParamAnnotation = method.getAnnotation(ApiImplicitParam.class);
        if (apiImplicitParamAnnotation != null
                && apiImplicitParamAnnotation.name().equals(parameter.getName())
                && StringUtils.hasText(apiImplicitParamAnnotation.value())) {
            return apiImplicitParamAnnotation.value();
        }
        ApiImplicitParams apiImplicitParamsAnnotation = method.getAnnotation(ApiImplicitParams.class);
        if (apiImplicitParamsAnnotation != null) {
            for (ApiImplicitParam itemApiImplicitParam : apiImplicitParamsAnnotation.value()) {
                // 如果在parameters里有 这个属性就返回
                if (itemApiImplicitParam.name().equals(parameter.getName())) {
                    if (StringUtils.hasText(itemApiImplicitParam.value())) {
                        return itemApiImplicitParam.value();
                    }
                    break;
                }
            }
        }





        return null;
    }
}