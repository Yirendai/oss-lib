package com.yirendai.oss.lib.swagger.model;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

import com.fasterxml.classmate.TypeResolver;

import lombok.Builder;
import lombok.Data;

import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.Parameter;

import java.util.List;

/**
 * Created on 16/12/1.
 * Desc: api request
 */
@Data
@Builder
public class ApiRequest {

  List<Parameter> parameters;

  /**
   * add a parameter.
   */
  public ApiRequest addParameter(final String name, final String description, final boolean required) {
    return addParameter(name, description, null, required, false);
  }

  public ApiRequest addParameter( //
    final String name, //
    final String description, //
    final String defaultValue, //
    final boolean required, //
    final boolean allowableValues //
  ) {
    if (this.parameters == null) {
      this.setParameters(newArrayList());
    }
    this.getParameters().add(new ParameterBuilder()
      .name(name)
      .description(description)
      .defaultValue(defaultValue)
      .required(required)
      .allowMultiple(false)
      .type(new TypeResolver().resolve(String.class))
      .modelRef(new ModelRef("string"))
      .allowableValues(!isNullOrEmpty(defaultValue) //
        ? new AllowableListValues(newArrayList(defaultValue), "string") : null)
      .parameterType("query")
      .build());
    return this;
  }
}
