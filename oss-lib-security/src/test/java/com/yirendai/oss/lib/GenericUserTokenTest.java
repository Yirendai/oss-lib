package com.yirendai.oss.lib;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirendai.oss.boot.autoconfigure.AppSecurityProperties;
import com.yirendai.oss.lib.common.Jackson2Utils;
import com.yirendai.oss.lib.common.crypto.Cryptos;
import com.yirendai.oss.lib.common.crypto.EncodeCipher;
import com.yirendai.oss.lib.common.crypto.Jwt;
import com.yirendai.oss.lib.common.crypto.KeyExpression;
import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.User;
import com.yirendai.oss.lib.security.api.UserDetails;
import com.yirendai.oss.lib.security.internal.BaseGrantedAuthority;
import com.yirendai.oss.lib.webmvc.api.JsonToken;
import com.yirendai.oss.lib.webmvc.api.JsonWebToken;
import com.yirendai.oss.lib.webmvc.api.SecureToken;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;
import com.yirendai.oss.lib.webmvc.api.UrlEncodedToken;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by zhanghaolun on 16/11/21.
 */
@Slf4j
public class GenericUserTokenTest {

  private ObjectMapper objectMapper;
  private AppSecurityProperties appSecurityProperties;
  private TypeSafeToken<GenericUser> token;

  @Before
  public void setUp() {
    this.appSecurityProperties = new AppSecurityProperties();
    this.appSecurityProperties.setCookieKey(new KeyExpression("AES256_CBC16:BpDSoeJkqinO93gkGe28UMRkhM5VKdM4OC0FZJC8LXM="));
    this.appSecurityProperties.setJwtKey(new KeyExpression("HS512:Ve+/vU5u77+977+977+977+977+9Acu/77+977+977+9OXrvv71XH++/vRLvv73vv73vv73vv71577+9fQLvv73vv71eB++/vW7vv71g77+977+977+9L++/vWDvv73vv73vv71577+9VO+/ve+/vR3vv73vv73Coemfv++/ve+/vQ=="));

    this.objectMapper = Jackson2Utils.setupObjectMapper(null, new ObjectMapper());

    TypeSafeToken<GenericUser> token = new JsonToken<>(GenericUser.class, this.objectMapper);

    final Jwt jwtCipher = Cryptos.cipher(this.appSecurityProperties.getJwtKey());
    if (jwtCipher != null) {
      token = new JsonWebToken<>(token, jwtCipher, 3600);
    } else {
      log.warn("INSECURE ! JwtKey not set. Using plain text token.");
    }

    final EncodeCipher cookieCipher = Cryptos.cipher(this.appSecurityProperties.getCookieKey());
    if (cookieCipher != null) {
      token = new SecureToken<>(token, cookieCipher);
    }

    this.token = new UrlEncodedToken<>(token);
  }

  @Test
  public void testGenericUserToken() {
    final User user = UserDetails.userDetailsBuilder()
      .authorities(Sets.newHashSet(new BaseGrantedAuthority("ADMIN"))) //
      .enabled(true) //
      .id("0") //
      .name("name") //
      .password("password") //
      .properties(ImmutableMap.of()) //
      .build();
    final GenericUser genericUser = GenericUser.fromUser(user);
    final String token = this.token.toToken(genericUser);
    log.info("token: {}", token);
    log.info("token.length: {}", token.length());
    final GenericUser fromToken = this.token.fromToken(token);
    assertEquals(fromToken, genericUser);
  }
}
