package it.com.yirendai.oss.lib.security;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Created by zhanghaolun on 16/10/29.
 */
@RestController
@RequestMapping(path = "/secured")
public class SecuredController {

  @RequestMapping(path = "/user", method = GET)
  public Principal getUser(final Principal principal) {
    return principal;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @RequestMapping(path = "/forAdminOnly", method = GET)
  public Principal forAdminOnly(final Principal principal) {
    return principal;
  }

  @PreAuthorize("hasPermission('DATA', 'VIEW')")
  @RequestMapping(path = "/forUserAndAdmin", method = GET)
  public Principal forUserAndAdmin(final Principal principal) {
    return principal;
  }

  @PreAuthorize("hasRole('USER')")
  @RequestMapping(path = "/forUserOnly", method = GET)
  public Principal forUserOnly(final Principal principal) {
    return principal;
  }
}
