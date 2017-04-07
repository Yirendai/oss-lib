package com.yirendai.oss.lib.swagger.model;

import lombok.Builder;
import lombok.Data;

/**
 * Created on 16/12/1.
 * Desc: api response
 */
@Data
@Builder
public class ApiResponse {

  private Class<?> response = Void.class;
}
