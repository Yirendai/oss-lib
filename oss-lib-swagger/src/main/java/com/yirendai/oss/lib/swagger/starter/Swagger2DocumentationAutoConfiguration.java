package com.yirendai.oss.lib.swagger.starter;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.yirendai.oss.lib.swagger.SwaggerUtils.apiInfo;
import static com.yirendai.oss.lib.swagger.starter.ManagementConfiguration.modelResolvedError;
import static com.yirendai.oss.lib.swagger.starter.NoManagementConfiguration.MANAGEMENT_PATHS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static springfox.documentation.builders.PathSelectors.ant;
import static springfox.documentation.builders.PathSelectors.regex;

import com.google.common.base.Predicate;

import com.fasterxml.classmate.ResolvedType;
import com.yirendai.oss.boot.autoconfigure.AppUtils;
import com.yirendai.oss.boot.autoconfigure.ConditionalOnNotEnvProduction;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.SpringfoxWebMvcConfiguration;
import springfox.documentation.spring.web.json.JacksonModuleRegistrar;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.configuration.SwaggerCommonConfiguration;
import springfox.documentation.swagger2.configuration.Swagger2JacksonModule;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Jin Yuliang on 16/8/18.
 */
@AutoConfigureAfter(SecurityAutoConfiguration.class)
@ConditionalOnNotEnvProduction
@ConditionalOnWebApplication
@Configuration
@Import({ //
  SpringfoxWebMvcConfiguration.class, //
  SwaggerCommonConfiguration.class, //
  ManagementConfiguration.class, //
  NoManagementConfiguration.class})
@ComponentScan(basePackages = { //
  "springfox.documentation.swagger2.readers.parameter", //
  "springfox.documentation.swagger2.web", //
  "springfox.documentation.swagger2.mappers"})
@Slf4j
public class Swagger2DocumentationAutoConfiguration {

  public static final String DOCKET_APPLICATION = "docketApplication";

  @Autowired(required = false)
  private ServerProperties serverProperties;

  @Qualifier(MANAGEMENT_PATHS)
  @Autowired
  public Predicate<String> managementPaths;

  @Bean
  public JacksonModuleRegistrar swagger2Module() {
    return new Swagger2JacksonModule();
  }

  @ConditionalOnMissingBean(name = DOCKET_APPLICATION)
  @Bean(name = DOCKET_APPLICATION)
  public Docket docketApplication() {
    final Docket docket = new Docket(DocumentationType.SWAGGER_2) //
      .apiInfo(apiInfo("application", "application's endpoints")) //
      .groupName("api") //
      .select() //
      .apis(or(this.applicationAips(), this.springApis())) //
      .paths(not(this.managementPaths)) //
      .build();
    final Optional<ResolvedType> modelResolvedError = modelResolvedError();
    return modelResolvedError.isPresent() ? docket.additionalModels(modelResolvedError.get()) : docket;
  }

  private Predicate<String> errorPath() {
    return ant(this.serverProperties.getError().getPath());
  }

  private Predicate<String> anyPath() {
    return regex("/.*");
  }

  private Predicate<RequestHandler> applicationAips() {
    final Predicate<RequestHandler> ossLibApi = RequestHandlerSelectors.basePackage("com.yirendai"); // TODO 从此类包取前两段
    final String applicationPackage = AppUtils.appBasePackage("");
    return isNotBlank(applicationPackage) ? //
      or(RequestHandlerSelectors.basePackage(applicationPackage), ossLibApi) : //
      RequestHandlerSelectors.any();
  }

  private Predicate<RequestHandler> springApis() {
    return RequestHandlerSelectors.basePackage("org.springframework");
  }

  @Configuration
  @ConditionalOnClass(WebSecurityConfigurerAdapter.class)
  @ConditionalOnBean(ObjectPostProcessor.class)
  @ConditionalOnProperty(prefix = "security.basic", name = "enabled", matchIfMissing = true)
  static class SwaggerSecurityConfiguration {

    @Bean
    public WebSecurityConfigurerAdapter swaggerSecurityConfigurer() {
      return new Swagger2DocumentationAutoConfiguration.SwaggerSecurityConfiguration.SwaggerSecurityConfigurer();
    }

    @Order(SecurityProperties.BASIC_AUTH_ORDER - 11)
    private static class SwaggerSecurityConfigurer extends WebSecurityConfigurerAdapter {

      @Autowired
      private Environment environment;

      @Override
      public void init(final WebSecurity web) throws Exception {
        final RequestMatcher requestMatcher = swaggerRequestMatcher();
        if (requestMatcher != null) {
          web.ignoring().requestMatchers(requestMatcher);
        }
        //super.init(web); // don't call super here or there will be a duplicate AnyRequestMatcher.
      }

      //@Override
      //public void configure(final HttpSecurity http) throws Exception {
      //  final RequestMatcher requestMatcher = swaggerRequestMatcher();
      //  if (requestMatcher != null) {
      //    http.requestMatcher(requestMatcher).authorizeRequests().anyRequest().permitAll();
      //  }
      //}

      private RequestMatcher swaggerRequestMatcher() {
        final Set<String> paths = newLinkedHashSet();
        // default is: /v2/api-docs
        paths.add("/webjars/**");
        paths.add(this.environment.getProperty("springfox.documentation.swagger.v1.path", "/v1/**"));
        paths.add(this.environment.getProperty("springfox.documentation.swagger.v2.path", "/v2/**"));
        paths.addAll(newArrayList("/swagger-ui.html", "/swagger-resources/**"));

        final RequestMatcher requestMatcher;
        if (!paths.isEmpty()) {
          final List<RequestMatcher> matchers = paths.stream() //
            .map(pattern -> new AntPathRequestMatcher(pattern, null)) //
            .collect(toList());
          requestMatcher = new OrRequestMatcher(matchers);
        } else {
          requestMatcher = null;
        }
        return requestMatcher;
      }
    }
  }
}
