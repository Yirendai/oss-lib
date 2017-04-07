package com.yirendai.oss.lib.security.starter;

import static com.yirendai.oss.boot.autoconfigure.AppSecurity.ENABLED;

import com.yirendai.oss.boot.autoconfigure.ConditionalOnAppSecurity;
import com.yirendai.oss.boot.autoconfigure.ConditionalOnNotEnvProduction;
import com.yirendai.oss.lib.security.swagger.AfterOperationBuilderBuildPlugin;
import com.yirendai.oss.lib.security.swagger.AuthenticationTokenHeaderBuilderPlugin;
import com.yirendai.oss.lib.security.swagger.BasicAuthHeaderBuilderPlugin;
import com.yirendai.oss.lib.security.swagger.SecurityApiDocumentationPlugin;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass(name = {"springfox.documentation.RequestHandler"})
@ConditionalOnNotEnvProduction
@Configuration
public class SwaggerConfiguration {

  @Bean
  @ConditionalOnAppSecurity(ENABLED)
  public AuthenticationTokenHeaderBuilderPlugin authenticationTokenHeaderBuilderPlugin() {
    return new AuthenticationTokenHeaderBuilderPlugin();
  }

  @Bean
  @ConditionalOnAppSecurity(ENABLED)
  public BasicAuthHeaderBuilderPlugin basicAuthHeaderBuilderPlugin() {
    return new BasicAuthHeaderBuilderPlugin();
  }

  @Bean
  @ConditionalOnAppSecurity(ENABLED)
  public SecurityApiDocumentationPlugin securityApiDocumentationPlugin() {
    return new SecurityApiDocumentationPlugin();
  }

  @Bean
  @ConditionalOnAppSecurity(ENABLED)
  public AfterOperationBuilderBuildPlugin afterOperationBuilderBuildPlugin() {
    return new AfterOperationBuilderBuildPlugin();
  }
}
