> Note: This gradle project if for macos and linux/unix only, windows is not supported

#### 发布到maven仓库

  发布oss-lib的快照

    VERSIONS=( "1.3.5.RELEASE" "1.3.6.RELEASE" "1.3.7.RELEASE" "1.4.1.RELEASE" )
    for version in "${VERSIONS[@]}"; do (gradle -Pinfrastructure=${INFRASTRUCTURE} -PspringBootVersion=${version} -PtestFailureIgnore=true clean build install uploadArchives); done
    
#### 本地安装

  本地安装oss-lib

      VERSIONS=( "1.3.5.RELEASE" "1.3.6.RELEASE" "1.3.7.RELEASE" "1.4.1.RELEASE" )
      for version in "${VERSIONS[@]}"; do (gradle -Pinfrastructure=${INFRASTRUCTURE} -PspringBootVersion=${version} -PtestFailureIgnore=true clean build install); done

#### Import into IDE

  Eclipse use 'buildship-gradle-integration' plugin.

#### Why windows is not supported

  Windows has problems on smlinks, see: https://github.com/git-for-windows/git/wiki/Symbolic-Links.
    
  ~~grant SeCreateSymbolicLinkPrivilege by [polsedit](http://polsedit.southsoftware.com/)~~
  ~~`fsutil behavior query SymlinkEvaluation`~~
  ~~make sure to run `git config --global core.symlinks true`~~
  ~~git-for-windows winsymlinks:native or winsymlinks:WFS_nativestrict ?~~
  ~~export MSYS2=winsymlinks:native~~
  ~~export MSYS=winsymlinks:native~~
  ~~export CYGWIN=winsymlinks:native~~

#### 参考文档

https://spring.io/blog/2015/11/25/migrating-spring-cloud-apps-from-spring-boot-1-2-to-1-3
https://spring.io/blog/2015/11/30/migrating-oauth2-apps-from-spring-boot-1-2-to-1-3
http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security-oauth2
https://spring.io/blog/2016/04/15/testing-improvements-in-spring-boot-1-4
[Upgrade to Spring Boot 1.4](https://my.oschina.net/hantsy/blog/720108)
