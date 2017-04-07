package com.yirendai.oss.boot.autoconfigure;

import static com.yirendai.oss.boot.autoconfigure.PathUtils.isManagementPath;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Created by zhanghaolun on 16/11/3.
 */
public class PathUtilsTest {

  @Test
  public void testPathUtils() {
    final String managementContextPath = "/admin";
    assertTrue(isManagementPath(managementContextPath, managementContextPath));
    assertTrue(isManagementPath(managementContextPath, managementContextPath + "/"));
    assertTrue(isManagementPath(managementContextPath, managementContextPath + "/env"));
    assertTrue(isManagementPath(managementContextPath, managementContextPath + ".json"));
  }
}
