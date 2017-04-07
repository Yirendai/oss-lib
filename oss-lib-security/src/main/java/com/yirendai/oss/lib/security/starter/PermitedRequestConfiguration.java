package com.yirendai.oss.lib.security.starter;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.yirendai.oss.boot.autoconfigure.AppType.RESOURCE;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.collect.ImmutableMap;

import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.boot.autoconfigure.AppSecurityProperties;
import com.yirendai.oss.lib.security.internal.VerifyCodeFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.SpringBootWebSecurityConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * see: {@link SpringBootWebSecurityConfiguration} IgnoredPathsWebSecurityConfigurerAdapter.
 * Created by zhanghaolun on 16/8/19.
 */
@Order(PermitedRequestConfiguration.ORDER_PERMITED_REQUEST)
@Configuration
public class PermitedRequestConfiguration extends SecurityConfigurerAdapter<PermitedRequestConfiguration> {

  public static final String PERMITED_REQUESTS = "permitedRequests";
  public static final String PERMITED_REQUEST_MATCHER = "permitedRequestMatcher";

  public static final int ORDER_PERMITED_REQUEST = SecurityProperties.IGNORED_ORDER + 1;

  @Autowired
  private Environment environment;

  @Autowired
  private AppProperties appProperties;

  @Autowired(required = false)
  private VerifyCodeFilter verifyCodeFilter;

  @Autowired(required = false)
  private H2ConsoleProperties h2ConsoleProperties;

  @Override
  public void init(final WebSecurity web) {
    final Set<String> ignored = newLinkedHashSet();
    // ! TODO ! 其它地方似乎也加了这些, 条件开启, 重复代码 see: Swagger2DocumentationAutoConfiguration.swaggerRequestMatcher
    // default is: /v2/api-docs
    //ignored.add("/webjars/**");
    //ignored.add(this.environment.getProperty("springfox.documentation.swagger.v1.path", "/v1/**"));
    //ignored.add(this.environment.getProperty("springfox.documentation.swagger.v2.path", "/v2/**"));
    //ignored.addAll(newArrayList("/swagger-ui.html", "/swagger-resources/**"));

    ignored.add("/h2-console/**");
    if (this.h2ConsoleProperties != null) {
      final String path = this.h2ConsoleProperties.getPath();
      final String antPattern = (path.endsWith("/") ? path + "**" : path + "/**");
      ignored.add(antPattern);
    }

    // create another filterChain after spring-boot's default (security.ignored) filterChain
    if (!ignored.isEmpty()) {
      final List<RequestMatcher> matchers = ignored.stream() //
        .map(pattern -> new AntPathRequestMatcher(pattern, null)) //
        .collect(toList());
      final RequestMatcher requestMatcher = new OrRequestMatcher(matchers);
      web.ignoring().requestMatchers(requestMatcher);
    }
  }

  @Override
  public void configure(final HttpSecurity http) {

  }

  @Bean(name = PERMITED_REQUEST_MATCHER)
  public RequestMatcher permitedRequestMatcher() {
    final RequestMatcher result;
    if (this.appProperties.getSecurityEnabled()) {
      // requestMatchers must contain a value
      final List<RequestMatcher> antMatchers = this.permitedRequests().entrySet().stream()
        .flatMap(entry -> entry.getValue().stream().map(pattern -> //
          isBlank(entry.getKey()) //
            ? new AntPathRequestMatcher(pattern) : new AntPathRequestMatcher(pattern, entry.getKey())
        )).collect(toList());
      result = antMatchers.isEmpty() ? request -> false : new OrRequestMatcher(antMatchers);
    } else {
      result = AnyRequestMatcher.INSTANCE;
    }
    return result;
  }

  @Bean(name = PERMITED_REQUESTS)
  public Map<String, List<String>> permitedRequests() {
    // permit的还是要过filter和authenticationManager
    final AppSecurityProperties security = this.appProperties.getSecurity();

    final String loginPage = security.getLoginPage();

    final Collection<String> defaultPermited = this.appProperties.getType() != RESOURCE ? //
      newLinkedHashSet(newArrayList( //
        security.getLoginPublicKeyUrl(), loginPage, security.getLoginProcessingUrl(), //
        security.getLogoutUrl() + "/*", security.getLogoutUrl() //
      )) : newArrayList();

    final Collection<String> permitedRequests = newLinkedHashSet();

    if (isNotBlank(security.getPermited())) {
      permitedRequests.addAll(newArrayList(security.getPermited().split("[ ]*,[ ]*")));
    }

    permitedRequests.addAll(defaultPermited);

    if (this.verifyCodeFilter != null) {
      permitedRequests.add(this.verifyCodeFilter.getCodeUrl());
    }

    final Map<String, List<String>> grouped = permitedRequests.stream()
      .map(expression -> {
        final String[] fragments = expression.split(":");
        return fragments.length == 1 //
          ? newArrayList("", fragments[0]) : newArrayList(fragments[0].toUpperCase(), fragments[1]);
      })
      .collect(groupingBy(o -> o.get(0), mapping(o -> o.get(1), toList())));

    return ImmutableMap.copyOf(grouped);
  }
}
