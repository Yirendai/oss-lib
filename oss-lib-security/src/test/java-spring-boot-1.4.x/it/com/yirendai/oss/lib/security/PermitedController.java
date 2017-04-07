package it.com.yirendai.oss.lib.security;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.yirendai.oss.boot.autoconfigure.AppProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Created by zhanghaolun on 16/10/31.
 */
@RestController
@RequestMapping(path = "/permited")
public class PermitedController {

  @Autowired
  private AppProperties appProperties;

  @RequestMapping(path = "/random", method = GET)
  public String random() {
    return this.appProperties.getRandom();
  }

  @RequestMapping(path = "/user", method = GET)
  public Principal getUser(final Principal principal) {
    return principal;
  }
}
