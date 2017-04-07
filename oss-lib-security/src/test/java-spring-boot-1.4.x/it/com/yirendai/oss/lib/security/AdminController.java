package it.com.yirendai.oss.lib.security;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.UserDetails;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/admin", produces = {APPLICATION_JSON_VALUE})
public class AdminController {

  @Autowired
  @Setter
  private UserService userService;

  @RequestMapping(path = "/users/{userId}", method = GET)
  public UserDetails user(@PathVariable(name = "userId") final Long userId) {
    final GenericUser genericUser = GenericUser.fromUser(this.userService.findById(userId));
    return genericUser.toUserInfo();
  }
}
