package com.yirendai.oss.lib.security.swagger;

import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Lists.newArrayList;
import static com.yirendai.oss.boot.autoconfigure.AppType.RESOURCE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static springfox.documentation.builders.PathSelectors.regex;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import com.fasterxml.classmate.TypeResolver;
import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.boot.autoconfigure.AppSecurityProperties;
import com.yirendai.oss.lib.common.crypto.KeyExpression;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.internal.rest.RestfulLoginPublicKeyFilter;
import com.yirendai.oss.lib.swagger.ManualRequestHandler;
import com.yirendai.oss.lib.swagger.SwaggerUtils;
import com.yirendai.oss.lib.swagger.model.ApiOperationInfo;
import com.yirendai.oss.lib.swagger.model.ApiRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.DefaultGenericTypeNamingStrategy;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.DocumentationPlugin;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spi.service.contexts.DocumentationContextBuilder;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * equivalent to @Bean public Docket securityDocket() {... }.
 */
@Order(value = SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class SecurityApiDocumentationPlugin implements DocumentationPlugin {

  @Autowired
  private TypeResolver typeResolver;
  @Autowired
  private AppProperties appProperties;

  @Override
  public DocumentationContext configure(final DocumentationContextBuilder documentationContextBuilder) {
    documentationContextBuilder
      .apiInfo(SwaggerUtils.apiInfo("oss-lib-security", "oss-lib-security's security endpoints"))
      .groupName("security")
      .pathMapping(Optional.absent())
      .genericsNaming(new DefaultGenericTypeNamingStrategy())
      .requestHandlers(this.requestHandlers())
      .additionalModels(Sets.newHashSet(this.typeResolver.resolve(ResolvedError.class)));
    return documentationContextBuilder.build();
  }

  private List<RequestHandler> requestHandlers() {
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();
    // TODO add oauth endpoints
    // TODO conditional filter
    // finish how to add description
    final List<RequestHandler> result = newArrayList();
    if (appSecurityProperties.getEnabled() && this.appProperties.getType() != RESOURCE) {
      result.add( // formAuth loginPage
        ManualRequestHandler.requestHandlerBuilder()
          .consumes(new ConsumesRequestCondition())
          .declaringClass(DefaultLoginPageGeneratingFilter.class)
          .groupName(DefaultLoginPageGeneratingFilter.class.getSimpleName())
          .headers(new HeadersRequestCondition())
          .parameters(newArrayList())
          .params(new ParamsRequestCondition())
          .patternsCondition(new PatternsRequestCondition(appSecurityProperties.getLoginPage()))
          .produces(new ProducesRequestCondition(TEXT_HTML_VALUE))
          .returnType(this.typeResolver.resolve(String.class))
          .supportedMethods(new RequestMethodsRequestCondition(GET))
          .build()
      );
      result.add( // formAuth loginProcessingUrl
        ManualRequestHandler.requestHandlerBuilder()
          .consumes(new ConsumesRequestCondition())
          .declaringClass(UsernamePasswordAuthenticationFilter.class)
          .groupName(UsernamePasswordAuthenticationFilter.class.getSimpleName())
          .headers(new HeadersRequestCondition())
          .parameters(newArrayList( //
            //  new ResolvedMethodParameter("username", null, TYPE_RESOLVER.resolve(String.class))
          ))
          .apiOperationInfo(ApiOperationInfo.builder()
            .notes("用户登录请求处理")
            .apiRequest(ApiRequest.builder()
              .parameters(newArrayList())
              .build()
              .addParameter("username", "enter your user name", true)
              .addParameter("password", "enter your password", true)
              .addParameter("_csrf", "Cross-site request forgery", false)
              .addParameter("remember-me", "So remember me?", false))
            .build()
          ).patternsCondition(new PatternsRequestCondition(appSecurityProperties.getLoginProcessingUrl()))
          .produces(new ProducesRequestCondition(APPLICATION_JSON_VALUE))
          .returnType(this.typeResolver.resolve(GenericUser.class))
          .supportedMethods(new RequestMethodsRequestCondition(POST))
          .build()

      );
      result.add( // formAuth loginPublicKeyUrl
        ManualRequestHandler.requestHandlerBuilder()
          .consumes(new ConsumesRequestCondition())
          .declaringClass(RestfulLoginPublicKeyFilter.class)
          .groupName(RestfulLoginPublicKeyFilter.class.getSimpleName())
          .headers(new HeadersRequestCondition())
          .parameters(newArrayList())
          .params(new ParamsRequestCondition())
          .patternsCondition(new PatternsRequestCondition(appSecurityProperties.getLoginPublicKeyUrl()))
          .produces(new ProducesRequestCondition(APPLICATION_JSON_VALUE))
          .returnType(this.typeResolver.resolve(KeyExpression.class))
          .supportedMethods(new RequestMethodsRequestCondition(GET))
          .build()
      );
      result.add( // formAuth logoutUrl
        ManualRequestHandler.requestHandlerBuilder()
          .consumes(new ConsumesRequestCondition())
          .declaringClass(LogoutFilter.class)
          .groupName(LogoutFilter.class.getSimpleName())
          .headers(new HeadersRequestCondition())
          .parameters(newArrayList())
          .params(new ParamsRequestCondition())
          .patternsCondition(new PatternsRequestCondition(appSecurityProperties.getLogoutUrl()))
          .produces(new ProducesRequestCondition(APPLICATION_JSON_VALUE))
          .returnType(this.typeResolver.resolve(Void.class))
          .supportedMethods(new RequestMethodsRequestCondition(POST))
          .build()
      );
    }
    return result;
  }

  @Override
  public DocumentationType getDocumentationType() {
    return DocumentationType.SWAGGER_2;
  }

  @Override
  public String getGroupName() {
    return "security";
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean supports(final DocumentationType delimiter) {
    return DocumentationType.SWAGGER_2.equals(delimiter);
  }

  /**
   * @return securityPaths
   * @deprecated remove this later.
   */
  @Deprecated
  public Predicate<String> securityPaths() {
    final String securityBasePath = this.appProperties.getSecurity().getBasePath();
    return or(regex(securityBasePath + "/.*"), regex("/oauth/.*"));
  }

  private static List<ResponseMessage> responseMessages() {
    final List<ResponseMessage> responseMessages = newArrayList();
    responseMessages.add(new ResponseMessageBuilder()
      .code(400).message("400diy")
      .responseModel(new ModelRef("com.yirendai.oss.lib.errorhandle.api.ResolvedError"))
      .build());
    return responseMessages;
  }

  /**
   * @return responseMessages
   * @deprecated remove this later.
   */
  @Deprecated
  static Map<RequestMethod, List<ResponseMessage>> additionalResponseMessages() {
    final Map<RequestMethod, List<ResponseMessage>> additionalResponseMessages = new EnumMap<>(RequestMethod.class);
    final List<ResponseMessage> responseMessages = responseMessages();
    for (final RequestMethod requestMethod : RequestMethod.values()) {
      additionalResponseMessages.put(requestMethod, responseMessages);
    }
    return additionalResponseMessages;
  }
}
