package com.yirendai.oss.lib.errorhandle.api;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Map;

/**
 * 应用抛出的通用错误信息.
 *
 * <p>
 * Created by zhanghaolun on 16/7/1.
 * </p>
 */
//@JsonInclude(JsonInclude.Include.NON_EMPTY) // for Jackson 2.x
//@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY) // for Jackson 1.x
//@XmlRootElement(name = "applicationError") // for JAXB
@EqualsAndHashCode(callSuper = false, of = {"status", "template"})
@Getter
public final class ApplicationException extends RuntimeException implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final String APPLICATION_EXCEPTION = "application exception";

  private final HttpStatus status;
  private final String template;
  private final Map<String, Serializable> contextVariables;

  public ApplicationException( //
    final HttpStatus status, //
    final String template, //
    final Map<String, Serializable> contextVariables //
  ) {
    super(APPLICATION_EXCEPTION);
    this.status = checkNotNull(status, "status must not null");
    this.template = checkNotNull(template, "template must not null");
    this.contextVariables = checkNotNull(contextVariables, "contextVariables must not null");
  }

  public static Boolean isApplicationError(final Throwable throwable) {
    return throwable != null && ApplicationException.class.isAssignableFrom(throwable.getClass());
  }

  public Map<String, Serializable> getContextVariables() {
    return this.contextVariables;
  }

  public HttpStatus getStatus() {
    return this.status;
  }

  public String getTemplate() {
    return this.template;
  }

  @Override
  public String toString() {
    final MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(ApplicationException.class)
      .add("status", this.status)
      .add("template", this.template);
    if (this.contextVariables != null) {
      this.contextVariables.entrySet().forEach(entry -> toStringHelper.add(entry.getKey(), entry.getValue()));
    }
    return toStringHelper.toString();
  }
}
