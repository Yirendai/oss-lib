
# oss-lib-test

支持测试的库, 改进了TestRestTemplate的兼容性, 以及其它小功能.

spring-boot-1.3.x升级到spring-boot-1.4.x时, 测试框架发生了比较大到变化, API不兼容.
但是oss-lib要针对多个spring-boot进行测试和构建, 所以建立一个`RestTemplateForTest`屏蔽spring-boot版本造成的差异.

另外提供小工具, 方便编写测试, 帮助测试代码保持紧凑.
