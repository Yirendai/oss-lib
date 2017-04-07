package com.yirendai.oss.lib.webmvc.starter;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.lib.webmvc.api.DomainResolver;
import com.yirendai.oss.lib.webmvc.api.RequestResolver;
import com.yirendai.oss.lib.webmvc.internal.DefaultDomainResolver;
import com.yirendai.oss.lib.webmvc.internal.DefaultHttpEntityMethodProcessor;
import com.yirendai.oss.lib.webmvc.internal.DefaultRequestResolver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties.Session;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.Servlet;

/**
 * Extends WebMvcConfigurerAdapter make this visable to MockMvc tests,
 * does not override it's method.
 */
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@ConditionalOnWebApplication
@AutoConfigureBefore({ErrorMvcAutoConfiguration.class, WebMvcAutoConfiguration.class})
@Configuration
@ComponentScan(basePackages = {"com.yirendai.oss.lib.webmvc.starter"})
@EnableConfigurationProperties(value = {AppProperties.class})
@Import({GsonConfiguration.class, Jackson2Configuration.class})
public class WebApplicationAutoConfiguration extends WebMvcConfigurerAdapter {

  @Autowired
  private ServerProperties serverProperties;

  @Autowired
  private AppProperties appProperties;

  @Autowired(required = false)
  private Environment environment;

  @Autowired(required = false)
  private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

  @Bean
  public DomainResolver domainResolver() {
    final Session.Cookie sessionCookie = this.serverProperties.getSession().getCookie();
    return new DefaultDomainResolver( //
      sessionCookie.getDomain(), //
      this.appProperties.getProdEnvironment() //
    );
  }

  // ------------------------------ default content negotiation ------------------------------

  @Bean
  public RequestResolver requestResolver() {
    return new DefaultRequestResolver(this.appProperties.getSecurity().getLoginEndpoint());
  }

  @Bean //TODO gson
  public DefaultHttpEntityMethodProcessor defaultHttpEntityMethodProcessor() {
    return DefaultHttpEntityMethodProcessor.defaultHttpEntityMethodProcessor(this.environment, this.objectMapper);
  }

  /**
   * Usage: domain = envDomain(this.env, domain, "");
   *
   * @param env                 env
   * @param rootDomain          domain.com
   * @param productionSubDomain productionSubDomain
   * @return domain
   */
  @Deprecated
  static String envDomain( //
    final String env, //
    final String rootDomain, //
    final String productionSubDomain //
  ) {
    final String domain;
    if (AppProperties.getProdEnvironment(env)) {
      domain = isNotBlank(productionSubDomain) ? //
        productionSubDomain + "." + rootDomain : //
        rootDomain;
    } else {
      domain = env + "." + rootDomain;
    }
    return domain;
  }
}
