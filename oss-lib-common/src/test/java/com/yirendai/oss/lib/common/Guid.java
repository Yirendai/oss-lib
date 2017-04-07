package com.yirendai.oss.lib.common;

@Deprecated
public final class Guid {

  // ------------------------------ 基于服务的GUID ------------------------------

  public static class GuidBuilder {

    // private String arg0;
    // private String arg1;

    public Guid build() {
      // 保证单例
      return null;
    }
  }

  public static GuidBuilder guidBuilder() {
    return new GuidBuilder();
  }

  private Guid() {

  }

  public Long guid() {
    return null;
  }

  // ------------------------------ 本地生成的GUID ------------------------------

  public static String randomGuid() {
    return null;
  }
}
