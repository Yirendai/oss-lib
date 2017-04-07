# lib-log4j2 开发文档

## 研发背景
Spring Boot Admin默认支持的日志实现是logback，对log4j2的支持不够友好，在log4j2中存在以下问题：  
1、不能可视化管理logger的loggerLevel；  
2、仅能管理少数几个配置中定义好的logger，不能管理庞大的不在配置中明确定义的logger的loggerlevel，这显然不能满足运营中的需要。   
为了扩展实现这些功能，故开发了此工程。

## 功能
+ 查询指定logger的等级
+ 修改任意logger及其等级

## 使用指南
通过Spring Boot Admin管理工程中的log4j2，需要引入此工程，具体引入配置如下：  

    <!-- 引入依赖管理 -->
    <dependencyManagement>
      <dependencies>
       <dependency>
         <groupId>com.yirendai.oss</groupId>
         <artifactId>oss-release-spring-boot-${spring-boot.version}</artifactId>
         <version>${oss-release.version}</version>
         <type>pom</type>
         <scope>import</scope>
       </dependency>
      </dependencies>
    </dependencyManagement>

    <!-- spring boot admin 管理需要 -->
    <dependency>
      <groupId>com.yirendai.oss.lib</groupId>
      <artifactId>oss-lib-adminclient-spring-boot-${spring-boot.version}</artifactId>
      <scope>runtime</scope>
    </dependency>
    <!-- 引入lib log4j2扩展  -->
    <dependency>
            <groupId>com.yirendai.oss.lib</groupId>
            <artifactId>oss-lib-log4j2-spring-boot-${spring-boot.version}</artifactId>
            <scope>runtime</scope>
    </dependency>
