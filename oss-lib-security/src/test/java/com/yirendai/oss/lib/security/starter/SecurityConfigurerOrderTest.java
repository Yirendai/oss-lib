package com.yirendai.oss.lib.security.starter;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.autoconfigure.security.SecurityProperties.DEFAULT_FILTER_ORDER;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Created by zhanghaolun on 16/10/28.
 */
@Slf4j
public class SecurityConfigurerOrderTest {

  @Order(SecurityConfigurerA.ORDER_A)
  public static class SecurityConfigurerA extends SecurityConfigurerAdapter<SecurityConfigurerA> {

    public static final int ORDER_A = HIGHEST_PRECEDENCE;

  }

  @Order(SecurityConfigurerB.ORDER_B)
  public static class SecurityConfigurerB extends SecurityConfigurerAdapter<SecurityConfigurerB> {

    public static final int ORDER_B = DEFAULT_FILTER_ORDER;
  }

  public static class SecurityConfigurerC extends SecurityConfigurerAdapter<SecurityConfigurerC> {

    public static final int ORDER_C = HIGHEST_PRECEDENCE;

    public int getOrder() {
      return SecurityConfigurerC.ORDER_C;
    }
  }

  public static class SecurityConfigurerD extends SecurityConfigurerAdapter<SecurityConfigurerD> {

  }

  @Test
  public void testGetOrder() {
    final SecurityConfigurer a = new SecurityConfigurerA();
    final SecurityConfigurer b = new SecurityConfigurerB();
    final SecurityConfigurer c = new SecurityConfigurerC();
    final SecurityConfigurer d = new SecurityConfigurerD();

    assertEquals(SecurityConfigurerA.ORDER_A, a.getOrder());
    assertEquals(SecurityConfigurerB.ORDER_B, b.getOrder());
    assertEquals(SecurityConfigurerC.ORDER_C, c.getOrder());
    assertEquals(Ordered.LOWEST_PRECEDENCE, d.getOrder());

    final List<SecurityConfigurer> configurers = newArrayList(a, b, c, d);
    final List<SecurityConfigurer> sorted = configurers.stream().sorted().collect(toList());

    log.info("a.getOrder() {}", a.getOrder());
    log.info("c.getOrder() {}", c.getOrder());
    log.info("b.getOrder() {}", b.getOrder());
    log.info("d.getOrder() {}", d.getOrder());

    assertTrue(sorted.get(0) == a || sorted.get(0) == c);
    assertTrue(sorted.get(1) == a || sorted.get(1) == c);
    assertEquals(b, sorted.get(2));
    assertEquals(d, sorted.get(3));
  }
}
