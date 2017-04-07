package com.yirendai.oss.lib.common.msginterpolate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Implementation of the {@link MessageInterpolator} that uses the Spring Expression Language (SpEL)
 * to evaluate expressions inside a template message.
 *
 * <p>
 * SpEL expressions are delimited by {@code #{} and {@code }}. The provided variables are accessible
 * directly by name.
 * </p>
 */
public class SpelMessageInterpolator implements MessageInterpolator {

  private static final Logger log = LoggerFactory.getLogger(SpelMessageInterpolator.class);

  private final EvaluationContext evalContext;

  /**
   * Creates a new instance with a custom {@link EvaluationContext}.
   *
   * @param evalContext evalContext
   */
  public SpelMessageInterpolator(final EvaluationContext evalContext) {
    Assert.notNull(evalContext, "EvaluationContext must not be null");
    this.evalContext = evalContext;
  }

  /**
   * Creates a new instance with {@link StandardEvaluationContext} including
   * {@link org.springframework.expression.spel.support.ReflectivePropertyAccessor
   * ReflectivePropertyAccessor} and {@link MapAccessor}.
   */
  public SpelMessageInterpolator() {
    final StandardEvaluationContext ctx = new StandardEvaluationContext();
    ctx.addPropertyAccessor(new MapAccessor());
    this.evalContext = ctx;
  }

  @Override
  public String interpolate(final String template, final Map<String, Object> variables) {
    Assert.notNull(template, "messageTemplate must not be null");

    String result;
    try {
      final Expression expression = parser().parseExpression(template, new TemplateParserContext());
      result = expression.getValue(this.evalContext, variables, String.class);
    } catch (final ExpressionException ex) {
      result = "Failed to interpolate message template, error: " + ex.getMessage(); // TODO test this
      log.warn("Failed to interpolate message template: {}, variables: {}", template, variables, ex);
    }
    return result;
  }

  ExpressionParser parser() {
    return new SpelExpressionParser();
  }
}
