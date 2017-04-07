package com.yirendai.oss.boot.autoconfigure;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Conditional} that only matches when the specified app type matches.
 *
 * @author haolunzhang
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnAppTypeCondition.class)
public @interface ConditionalOnAppType {

  AppType[] value() default {};
}
