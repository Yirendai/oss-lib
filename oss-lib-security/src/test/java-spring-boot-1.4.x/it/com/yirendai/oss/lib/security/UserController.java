package it.com.yirendai.oss.lib.security;

import static com.yirendai.oss.lib.security.api.GenericUser.fromPrincipal;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.UserDetails;

import io.swagger.annotations.ApiOperation;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Created by zhanghaolun on 16/10/29.
 */
@RestController
@RequestMapping(path = "/users", produces = {APPLICATION_JSON_VALUE})
@Slf4j
public class UserController {

  @Autowired
  @Setter
  private UserService userService;

  @ApiOperation(value = "当前用户", httpMethod = "GET", response = UserDetails.class, notes = "UserDetails数据")
  @RequestMapping(path = "/current", method = GET)
  @ResponseBody
  public UserDetails user(final Principal principal) {
    if (log.isDebugEnabled()) {
      log.debug("user principal: {}, type: {}", principal, (principal != null ? principal.getClass() : "null"));
    }

    final GenericUser genericUser = fromPrincipal(principal);
    return genericUser != null ? genericUser.toUserInfo() : null;
  }

  @RequestMapping(path = "/user/{userId}", method = GET)
  public UserDetails user(@PathVariable(name = "userId") final Long userId) {
    final GenericUser genericUser = GenericUser.fromUser(this.userService.findById(userId));
    return genericUser.toUserInfo();
  }
}
