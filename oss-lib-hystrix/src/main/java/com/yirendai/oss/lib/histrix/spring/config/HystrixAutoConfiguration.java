package com.yirendai.oss.lib.histrix.spring.config;

import com.netflix.hystrix.Hystrix;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto configuration for Hystrix.
 */
@Configuration
@ConditionalOnClass({Hystrix.class})
@ComponentScan(basePackages = "com.yirendai.oss.lib.histrix.config")
public class HystrixAutoConfiguration {
}
