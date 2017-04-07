package com.yirendai.oss.lib.webmvc.internal;

import static lombok.AccessLevel.PACKAGE;

import com.yirendai.oss.lib.webmvc.api.DomainResolver;

import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhanghaolun on 16/8/18.
 */
@Getter
@Setter(PACKAGE)
public class DefaultDomainResolver implements DomainResolver {

  private String domain;
  private Boolean prodEnvironment;

  public DefaultDomainResolver(final String domain, final Boolean prodEnvironment) {
    this.domain = domain != null ? domain : "";
    this.prodEnvironment = prodEnvironment;
  }

  @Override
  public String resolveDomain(final HttpServletRequest request) {
    final String serverName = request.getServerName();
    final String domain;
    if (this.domain.equals(serverName) || !this.prodEnvironment) {
      // domain = request.getServerPort() != 80 ? serverName + ":" + request.getServerPort() :
      // serverName;
      domain = "";
    } else {
      domain = this.domain;
    }
    return domain;
  }
}
