package com.yirendai.oss.lib.errorhandle.internal.rpc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhanghaolun on 16/7/12.
 */
@Configuration
@ConditionalOnClass(name = { //
  //"org.springframework.cloud.client.actuator.HasFeatures", //
  //"org.springframework.cloud.context.named.NamedContextFactory", //
  "feign.Feign"})
public class FeignErrorDecoderConfiguration {

  @Bean
  public GenericFeignErrorDecoder errorDecoder() {
    return new GenericFeignErrorDecoder();
  }
}
