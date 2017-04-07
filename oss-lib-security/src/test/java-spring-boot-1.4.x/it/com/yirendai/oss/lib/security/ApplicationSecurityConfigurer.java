package it.com.yirendai.oss.lib.security;

import com.yirendai.oss.lib.security.starter.SecurityConfigurerAdapter;

import lombok.SneakyThrows;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * To test .access expression, access: http://127.0.0.1:8080:8080/api/users/user/2
 * To test .hasRole(UserRole.ROLE_ADMIN), access: http://127.0.0.1:8080:8080/api/admin/users/2
 */
@Configuration
public class ApplicationSecurityConfigurer extends SecurityConfigurerAdapter<ApplicationSecurityConfigurer> {

  @SneakyThrows
  @Override
  public void configure(final HttpSecurity http) {
    http.authorizeRequests() //
      .antMatchers(HttpMethod.GET, "/admin/**") //
      .hasRole(UserRole.ROLE_ADMIN) //
      .antMatchers("/users/user/{userId}/**") // see: AccessControl
      .access("@accessControl.checkUserId(authentication,#userId)"); //
  }
}
