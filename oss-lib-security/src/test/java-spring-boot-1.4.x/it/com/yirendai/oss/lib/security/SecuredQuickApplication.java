package it.com.yirendai.oss.lib.security;

import com.yirendai.oss.boot.autoconfigure.AppUtils;
import com.yirendai.oss.lib.security.AllowAllPermissionEvaluator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;

@SpringBootApplication(exclude = {OAuth2AutoConfiguration.class})
public class SecuredQuickApplication {

  static {
    System.setProperty("spring.config.name", "application-quick");
  }

  public static void main(final String... args) throws Exception {
    final ApplicationContext ctx = SpringApplication.run(SecuredQuickApplication.class, args);
    AppUtils.printApplicationInfo(ctx);
  }

  /**
   * trigger on method security.
   *
   * @return permissionEvaluator
   */
  @Bean
  public PermissionEvaluator projectPermissionEvaluator() { //
    return new AllowAllPermissionEvaluator();
  }
}
