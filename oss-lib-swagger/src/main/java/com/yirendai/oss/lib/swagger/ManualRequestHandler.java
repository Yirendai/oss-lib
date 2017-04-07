package com.yirendai.oss.lib.swagger;

import static com.google.common.collect.Sets.newHashSet;
import static lombok.AccessLevel.PRIVATE;

import com.google.common.base.Optional;

import com.fasterxml.classmate.ResolvedType;
import com.yirendai.oss.lib.swagger.model.ApiOperationInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import springfox.documentation.RequestHandler;
import springfox.documentation.RequestHandlerKey;
import springfox.documentation.service.ResolvedMethodParameter;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

@Builder(builderMethodName = "requestHandlerBuilder")
@AllArgsConstructor(access = PRIVATE)
public class ManualRequestHandler implements RequestHandler {

  @Getter
  private final ApiOperationInfo apiOperationInfo;

  private final ConsumesRequestCondition consumes;
  private final Class<?> declaringClass;
  private final String groupName;
  private final HeadersRequestCondition headers;
  /**
   * method parameters.
   */
  private final List<ResolvedMethodParameter> parameters;
  /**
   * params condition expression.
   */
  private final ParamsRequestCondition params;
  /**
   * path exporession.
   */
  private final PatternsRequestCondition patternsCondition;
  private final ProducesRequestCondition produces;
  private final ResolvedType returnType;
  private final RequestMethodsRequestCondition supportedMethods;

  @Override
  public Class<?> declaringClass() {
    return this.declaringClass;
  }

  @Override
  public boolean isAnnotatedWith(final Class<? extends Annotation> clazz) {
    return false;
  }

  @Override
  public PatternsRequestCondition getPatternsCondition() {
    return this.patternsCondition;
  }

  @Override
  public String groupName() {
    return this.groupName;
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

  @Override
  public Set<RequestMethod> supportedMethods() {
    return this.supportedMethods != null ? this.supportedMethods.getMethods() : newHashSet();
  }

  @Override
  public Set<? extends MediaType> produces() {
    return this.produces != null ? this.produces.getProducibleMediaTypes() : newHashSet();
  }

  @Override
  public Set<? extends MediaType> consumes() {
    return this.consumes != null ? this.consumes.getConsumableMediaTypes() : newHashSet();
  }

  @Override
  public Set<NameValueExpression<String>> headers() {
    return this.headers != null ? this.headers.getExpressions() : newHashSet();
  }

  @Override
  public Set<NameValueExpression<String>> params() {
    return this.params != null ? this.params.getExpressions() : newHashSet();
  }

  @Override
  public <T extends Annotation> Optional<T> findAnnotation(final Class<T> clazz) {
    return Optional.absent();
  }

  @Override
  public RequestHandlerKey key() {
    return new RequestHandlerKey(
      this.getPatternsCondition().getPatterns(),
      this.supportedMethods(),
      this.consumes(),
      this.produces());
  }

  @Override
  public List<ResolvedMethodParameter> getParameters() {
    return this.parameters;
  }

  @Override
  public ResolvedType getReturnType() {
    return this.returnType;
  }

  @Override
  public <T extends Annotation> Optional<T> findControllerAnnotation(final Class<T> clazz) {
    return Optional.absent();
  }

  @Deprecated
  @Override
  public RequestMappingInfo getRequestMapping() {
    return new RequestMappingInfo(
      this.patternsCondition,
      this.supportedMethods,
      this.params,
      this.headers,
      this.consumes,
      this.produces,
      null
    );
  }

  @Deprecated
  @Override
  public HandlerMethod getHandlerMethod() {
    return null;
  }

  @Override
  public RequestHandler combine(final RequestHandler requestHandler) {
    return this;
  }
}
