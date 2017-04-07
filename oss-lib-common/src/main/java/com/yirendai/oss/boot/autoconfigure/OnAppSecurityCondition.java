package com.yirendai.oss.boot.autoconfigure;

import static com.yirendai.oss.boot.autoconfigure.AppSecurityProperties.APP_SECURITY;
import static com.yirendai.oss.boot.autoconfigure.AppSecurityProperties.DEFAULT_APP_SECURITY_ENABLED;
import static java.lang.Boolean.parseBoolean;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

@Slf4j
public class OnAppSecurityCondition extends SpringBootCondition {

  @Override
  public ConditionOutcome getMatchOutcome( //
    final ConditionContext context, //
    final AnnotatedTypeMetadata metadata //
  ) {
    final MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes( //
      ConditionalOnAppSecurity.class.getName(), true //
    );

    final AppSecurity value = (AppSecurity) attributes.get("value").get(0);

    final Environment environment = context.getEnvironment();
    final String key = APP_SECURITY + ".enabled";
    final Boolean securityEnabled = parseBoolean( //
      environment.getProperty(key, DEFAULT_APP_SECURITY_ENABLED.toString()) //
    );

    final ConditionOutcome outcome;
    if (value == AppSecurity.ENABLED) {
      outcome = securityEnabled ? ConditionOutcome.match() : //
        ConditionOutcome.noMatch("condition " + key + "=true not match.");
    } else {
      outcome = !securityEnabled ? ConditionOutcome.match() : //
        ConditionOutcome.noMatch("condition " + key + "=false not match.");
    }
    return outcome;
  }
}
