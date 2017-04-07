package com.yirendai.oss.boot.autoconfigure;

import static com.yirendai.oss.boot.autoconfigure.AppProperties.DEFAULT_APP_TYPE;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

@Slf4j
public class OnAppTypeCondition extends SpringBootCondition {

  public static Boolean matches(final AppType appType, final AppType... appTypes) {
    return ArrayUtils.contains(appTypes, appType);
  }

  @Override
  public ConditionOutcome getMatchOutcome( //
    final ConditionContext context, //
    final AnnotatedTypeMetadata metadata //
  ) {
    final MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes( //
      ConditionalOnAppType.class.getName(), true //
    );
    final AppType[] value = (AppType[]) attributes.get("value").get(0);

    final Environment environment = context.getEnvironment();
    final String key = "app.type";
    final AppType appType = AppType.valueOf(environment.getProperty(key, DEFAULT_APP_TYPE.name()).toUpperCase());
    final Boolean matches = OnAppTypeCondition.matches(appType, value);

    final ConditionOutcome outcome;
    if (matches) {
      outcome = ConditionOutcome.match();
    } else {
      outcome = ConditionOutcome.noMatch( //
        key + " not match. current: " + appType + ", condition: " + ArrayUtils.toString(value) //
      );
    }
    return outcome;
  }
}
