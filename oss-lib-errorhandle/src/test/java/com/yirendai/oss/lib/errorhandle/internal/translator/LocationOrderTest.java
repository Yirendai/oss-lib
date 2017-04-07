package com.yirendai.oss.lib.errorhandle.internal.translator;

import static com.yirendai.oss.boot.autoconfigure.AppErrorProperties.SearchStrategy.HIERARCHY_FIRST;
import static com.yirendai.oss.boot.autoconfigure.AppErrorProperties.SearchStrategy.ORDER_FIRST;
import static com.yirendai.oss.lib.errorhandle.internal.translator.DefaultExceptionTranslator.DEFAULT_EXCEPTION_CLASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;

import com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator;
import com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator.Location;
import com.yirendai.oss.lib.errorhandle.starter.ErrorHandleAutoConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Optional;

import javax.validation.ConstraintViolationException;

/**
 * Created by zhanghaolun on 16/10/25.
 */
@Slf4j
public class LocationOrderTest {

  private ExceptionTranslator hierarchyFirstTranslator;
  private ExceptionTranslator orderFirstTranslator;

  @Before
  public void setUp() {
    this.hierarchyFirstTranslator = ErrorHandleAutoConfiguration.buildExceptionTranslator(HIERARCHY_FIRST);
    this.orderFirstTranslator = ErrorHandleAutoConfiguration.buildExceptionTranslator(ORDER_FIRST);
  }

  @Test
  public void testNoHandlerFoundExceptionLocationOrder() {
    final Throwable throwable = new NoHandlerFoundException("GET", "http://127.0.0.1:8080", new HttpHeaders());

    final Optional<Location> hierarchyFirst = this.hierarchyFirstTranslator.find(throwable);
    log.info("hierarchyFirst: {}", hierarchyFirst);
    assertTrue(hierarchyFirst.isPresent());
    assertEquals(ImmutableSet.of("classpath:/errorhandle/default"), getSource(hierarchyFirst.get()).getBasenameSet());
    assertEquals("org.springframework.web.servlet.NoHandlerFoundException", hierarchyFirst.get().getKey());
    assertEquals(0L, (long) hierarchyFirst.get().getLevel());

    final Optional<Location> orderFirst = this.orderFirstTranslator.find(throwable);
    log.info("orderFirst: {}", orderFirst);
    assertTrue(orderFirst.isPresent());
    assertEquals(ImmutableSet.of("classpath:/errorhandle/application"), getSource(orderFirst.get()).getBasenameSet());
    assertEquals("javax.servlet.ServletException", orderFirst.get().getKey());
    assertEquals(1L, (long) orderFirst.get().getLevel());
  }

  @Test
  public void testConstraintViolationExceptionLocationOrder() {
    final Throwable throwable = new ConstraintViolationException("empty", ImmutableSet.of());

    final Optional<Location> hierarchyFirst = this.hierarchyFirstTranslator.find(throwable);
    assertTrue(hierarchyFirst.isPresent());
    assertEquals(ImmutableSet.of("classpath:/errorhandle/default"), getSource(hierarchyFirst.get()).getBasenameSet());
    assertEquals("javax.validation.ConstraintViolationException", hierarchyFirst.get().getKey());
    assertEquals(0, hierarchyFirst.get().getLevel().intValue());

    final Optional<Location> orderFirst = this.orderFirstTranslator.find(throwable);
    log.info("orderFirst: {}", orderFirst);
    assertTrue(orderFirst.isPresent());
    assertEquals(ImmutableSet.of("classpath:/errorhandle/application"), getSource(orderFirst.get()).getBasenameSet());
    assertEquals(DEFAULT_EXCEPTION_CLASS, orderFirst.get().getKey());
    assertEquals(Integer.MAX_VALUE, orderFirst.get().getLevel().intValue());
  }

  private static AbstractResourceBasedMessageSource getSource(final Location location) {
    return (AbstractResourceBasedMessageSource) location.getSource();
  }
}
