package com.yirendai.oss.lib.adminclient.starter;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.lib.common.crypto.Cryptos;
import com.yirendai.oss.lib.common.crypto.EncodeEncryptor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Created by Yuliang jin on 16/10/25.
 * TODO: Comments
 */
@ConditionalOnClass(SecurityProperties.class)
@AutoConfigureAfter(SecurityAutoConfiguration.class)
@Component
@ConditionalOnWebApplication
@Configuration
@EnableDiscoveryClient
@EnableConfigurationProperties(value = {AppProperties.class})
@Slf4j
public class AdminClientAutoConfiguration implements InfoContributor {

  public static final String ENCRYPTED = "encrypted";
  public static final String USER_NAME = "username";
  @SuppressWarnings("squid:S2068")
  public static final String PASSWORD = "password";
  public static final String MANAGEMENT_AUTHENTICATION = "managementAuthentication";

  @Autowired(required = false)
  private SecurityProperties securityProperties;

  private EncodeEncryptor encryptor;

  @Autowired
  public void setAppProperties(final AppProperties appProperties) {
    if (appProperties.getSecurityEnabled() && appProperties.getAdminPublicKey().isPresent()) {
      this.encryptor = Cryptos.encryptor(appProperties.getAdminPublicKey());
    }
  }

  @Override
  public void contribute(final Info.Builder builder) {
    if (this.securityProperties != null && this.securityProperties.getBasic().isEnabled()) {
      final SecurityProperties.User user = this.securityProperties.getUser();
      final HashMap<String, Object> authMap = new HashMap<>();
      if (this.encryptor != null) {
        authMap.put(ENCRYPTED, TRUE);
        authMap.put(USER_NAME, this.encryptor.encrypt(user.getName()));
        authMap.put(PASSWORD, this.encryptor.encrypt(user.getPassword()));
      } else {
        authMap.put(ENCRYPTED, FALSE);
        authMap.put(USER_NAME, user.getName());
        authMap.put(PASSWORD, user.getPassword());
      }
      builder.withDetail(MANAGEMENT_AUTHENTICATION, authMap);
    }
  }
}
