package com.yirendai.oss.boot.autoconfigure;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.yirendai.oss.boot.autoconfigure.AppType.MIXED;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.yirendai.oss.lib.common.crypto.KeyExpression;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * <p>
 * Configuration properties for spring-boot applications. see:
 * {@link org.springframework.boot.autoconfigure.web.ServerProperties}
 * </p>
 * Created by zhanghaolun on 16/8/17.
 */
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = true)
@SuppressWarnings({"PMD.ImmutableField", "PMD.SingularField", "PMD.AvoidUsingHardCodedIP"})
@Getter
public class AppProperties implements InitializingBean {

  public static final Pattern PROFILE_PATTERN = Pattern.compile("[a-zA-Z0-9.]+");
  public static final String DOT_ENV = ".env";
  public static final String PRODUCTION = "production";
  public static final String PRODUCTION_ENV = PRODUCTION + DOT_ENV;
  public static final String CI = "ci";
  public static final String CI_ENV = CI + DOT_ENV;
  public static final String DEVELOPMENT = "development";
  public static final String DEVELOPMENT_ENV = DEVELOPMENT + DOT_ENV;

  public static final AppType DEFAULT_APP_TYPE = MIXED;

  @Setter
  private AppType type;

  @NestedConfigurationProperty
  @Setter
  private AppLogProperties log = new AppLogProperties();

  /**
   * auto find out from spring.profiles.active
   */
  private String env;

  @NestedConfigurationProperty
  @Setter
  private AppErrorProperties error = new AppErrorProperties();

  @NestedConfigurationProperty
  @Setter
  private AppSecurityProperties security = new AppSecurityProperties();

  @Setter
  private KeyExpression adminPublicKey = new KeyExpression();

  //@Value("${random.int(999)}")
  @Value("${random.value}")
  private String random;

  public AppProperties() {
    this.type = DEFAULT_APP_TYPE;
  }

  static Optional<String> findEnv(final Environment environment) {
    final Collection<String> activeProfiles = asList(environment.getActiveProfiles());
    final Collection<String> envProfiles = newLinkedHashSet(activeProfiles).stream() //
      .filter(profile -> profile.endsWith(DOT_ENV)) //
      .map(profile -> profile.substring(0, profile.length() - 4)).collect(toList());
    checkArgument(envProfiles.size() < 2, "only 1 env is allowed, there are %s", envProfiles);
    return Optional.ofNullable(!envProfiles.isEmpty() ? envProfiles.iterator().next() : null);
  }

  public static String getEnvironment(final Environment environment) {
    final Optional<String> envOptional = findEnv(environment);
    return envOptional.isPresent() ? envOptional.get() : DEVELOPMENT;
  }

  public static Boolean getProdEnvironment(final String env) {
    return PRODUCTION.equals(env);
  }

  public Boolean getProdEnvironment() {
    return getProdEnvironment(this.env);
  }

  @Autowired
  public void setEnvironment(final Environment environment) {
    final boolean allMatch = Arrays.stream(environment.getActiveProfiles())
      .allMatch(activeProfile -> PROFILE_PATTERN.matcher(activeProfile).matches());
    checkArgument(allMatch, "profile name must match %s", PROFILE_PATTERN.pattern());
    this.env = getEnvironment(environment);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
  }

  public AppType getType() {
    return this.type;
  }

  public String getEnv() {
    return this.env;
  }

  public AppSecurityProperties getSecurity() {
    return this.security;
  }

  public String getSecurityDefaultTestUser() {
    checkState(!this.getProdEnvironment() || isBlank(this.security.getDefaultTestUser()), //
      "do not set app.security.defaultTestUser in env %s", PRODUCTION);
    return this.security.getDefaultTestUser();
  }

  public Boolean getSecurityEnabled() {
    return this.security.getEnabled();
  }

  public Boolean getSecurityUseTestUser() {
    return isNotBlank(getSecurityDefaultTestUser());
  }
}
