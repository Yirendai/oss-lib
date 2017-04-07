package com.yirendai.oss.boot.autoconfigure;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Conditional} that matches when the app security are enabled or disabled.
 *
 * @author haolunzhang
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnAppSecurityCondition.class)
public @interface ConditionalOnAppSecurity {

  AppSecurity value() default AppSecurity.ENABLED;
}
