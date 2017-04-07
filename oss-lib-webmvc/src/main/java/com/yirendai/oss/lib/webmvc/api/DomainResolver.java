package com.yirendai.oss.lib.webmvc.api;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhanghaolun on 16/8/18.
 */
public interface DomainResolver {

  String resolveDomain(HttpServletRequest request);
}
