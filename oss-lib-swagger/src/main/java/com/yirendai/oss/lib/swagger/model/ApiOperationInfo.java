package com.yirendai.oss.lib.swagger.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Created on 16/12/1.
 * Desc: manual custom some description about api info
 */
@Data
@Builder
public class ApiOperationInfo {

  private String name; // 名称
  private String notes; // 描述
  private String[] tags; // 标签
  private Map<String, String> extensions; // 扩展属性
  private ApiRequest apiRequest; // api请求
  private ApiResponse apiResponse; // api响应
}
