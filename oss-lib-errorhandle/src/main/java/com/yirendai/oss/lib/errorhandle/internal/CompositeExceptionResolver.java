package com.yirendai.oss.lib.errorhandle.internal;

import com.yirendai.oss.lib.errorhandle.api.ConcreteExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.errorhandle.internal.resolver.DefaultExceptionResolver;
import com.yirendai.oss.lib.errorhandle.starter.CompositeExceptionResolverFactoryBean;

import org.springframework.web.context.request.RequestAttributes;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * A {@link ExceptionResolver ExceptionResolver} that resolves exceptions through the provided
 * {@link ExceptionResolver ExceptionResolvers}.
 *
 * @see CompositeExceptionResolverFactoryBean
 */
public class CompositeExceptionResolver implements ExceptionResolver<Throwable> {

  private DefaultExceptionResolver defaultResolver;
  @SuppressWarnings("rawtypes")
  private Map<Class<? extends Throwable>, ConcreteExceptionResolver> resolverMap;

  @Override
  public ResolvedError resolve(final HttpServletRequest request, final Throwable throwable) {
    final ResolvedError resolvedError;

    final Optional<ExceptionResolver<Throwable>> resolverOptional = resolver(throwable);
    if (resolverOptional.isPresent()) {
      final ExceptionResolver<Throwable> resolver = resolverOptional.get();
      final ResolvedError resolved = resolver.resolve(request, throwable);
      if (resolved != null) {
        resolvedError = resolved;
      } else {
        resolvedError = this.defaultResolver.resolve(request, throwable);
      }
    } else {
      resolvedError = this.defaultResolver.resolve(request, throwable);
    }

    return resolvedError;
  }

  @Override
  public ResolvedError resolve(final RequestAttributes requestAttributes, final Throwable throwable) {
    final ResolvedError resolvedError;

    final Optional<ExceptionResolver<Throwable>> resolverOptional = resolver(throwable);
    if (resolverOptional.isPresent()) {
      final ExceptionResolver<Throwable> resolver = resolverOptional.get();
      final ResolvedError resolved = resolver.resolve(requestAttributes, throwable);
      if (resolved != null) {
        resolvedError = resolved;
      } else {
        resolvedError = this.defaultResolver.resolve(requestAttributes, throwable);
      }
    } else {
      resolvedError = this.defaultResolver.resolve(requestAttributes, throwable);
    }

    return resolvedError;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  Optional<ExceptionResolver<Throwable>> resolver(final Throwable throwable) {
    ExceptionResolver<Throwable> result = null;
    if (throwable != null) {
      for (Class type = throwable.getClass(); type != Throwable.class; type = type.getSuperclass()) {
        if (this.resolverMap.containsKey(type)) {
          result = this.resolverMap.get(type);
          break;
        }
      }
    }
    return Optional.ofNullable(result);
  }

  public void setDefaultResolver(final DefaultExceptionResolver defaultResolver) {
    this.defaultResolver = defaultResolver;
  }

  @SuppressWarnings("rawtypes")
  public void setResolverMap(final Map<Class<? extends Throwable>, ConcreteExceptionResolver> resolverMap) {
    this.resolverMap = resolverMap;
  }
}
