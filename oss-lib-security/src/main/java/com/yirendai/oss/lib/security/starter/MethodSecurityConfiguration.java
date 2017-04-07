package com.yirendai.oss.lib.security.starter;

import static com.yirendai.oss.boot.autoconfigure.AppSecurity.ENABLED;

import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.boot.autoconfigure.ConditionalOnAppSecurity;
import com.yirendai.oss.lib.security.MethodSecurityPermissionEvaluator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * Created by zhanghaolun on 16/7/13.
 */
@EnableGlobalMethodSecurity(jsr250Enabled = true, securedEnabled = true, prePostEnabled = true)
@ConditionalOnAppSecurity(ENABLED)
@ConditionalOnBean(value = {PermissionEvaluator.class})
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

  @Autowired
  private AppProperties appProperties;

  @Autowired(required = false)
  private PermissionEvaluator projectPermissionEvaluator;

  @Override
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    final PermissionEvaluator permissionEvaluator;
    if (this.projectPermissionEvaluator != null) {
      final MethodSecurityPermissionEvaluator methodSecurityPermissionEvaluator =
        new MethodSecurityPermissionEvaluator();
      methodSecurityPermissionEvaluator.setSecurityEnabled(this.appProperties.getSecurityEnabled());
      methodSecurityPermissionEvaluator.setProjectPermissionEvaluator(this.projectPermissionEvaluator);
      permissionEvaluator = methodSecurityPermissionEvaluator;
    } else {
      permissionEvaluator = new DenyAllPermissionEvaluator();
    }

    final DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
    expressionHandler.setPermissionEvaluator(permissionEvaluator);
    return expressionHandler;
  }
}
