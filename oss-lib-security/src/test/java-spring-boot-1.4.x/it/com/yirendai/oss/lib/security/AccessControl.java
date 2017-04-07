package it.com.yirendai.oss.lib.security;

import static com.yirendai.oss.lib.errorhandle.api.ApplicationExceptions.check;
import static com.yirendai.oss.lib.errorhandle.api.ApplicationExceptions.checkNotNull;
import static java.lang.Boolean.TRUE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.yirendai.oss.lib.security.api.GenericUser;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AccessControl {

  public Boolean checkUserId(final Authentication authentication, final Long id) {
    checkNotNull(authentication, UNAUTHORIZED, "authentication not present");
    checkNotNull(id, UNAUTHORIZED, "id not present");

    final Object principal = authentication.getPrincipal();
    checkNotNull(principal, UNAUTHORIZED, "principal not present");
    check(principal instanceof GenericUser, UNAUTHORIZED, "unsupported principal type {}", principal.getClass());

    final GenericUser user = (GenericUser) principal;
    check(user.getId().equals(id.toString()), UNAUTHORIZED, "user id not match");
    return TRUE;
  }
}
