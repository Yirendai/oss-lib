package com.yirendai.oss.lib.errorhandle.internal.resolver;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static org.springframework.util.StringUtils.isEmpty;

import com.yirendai.oss.lib.errorhandle.api.AbstractConcreteExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ValidationError;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Path.Node;

@Slf4j
public class ConstraintViolationExceptionResolver
  extends AbstractConcreteExceptionResolver<ConstraintViolationException> {

  static String convertToString( //
    final ConversionService conversionService, //
    final Object value //
  ) {
    String reuslt;
    if (value == null) {
      reuslt = null;
    } else if (conversionService == null) {
      reuslt = value.toString();
    } else {
      try {
        reuslt = conversionService.convert(value, String.class);
      } catch (final ConversionException ex) {
        if (log.isDebugEnabled()) {
          log.debug("failed to convert value '{}' to String.class.", value, ex);
        }
        reuslt = value.toString();
      }
    }
    return reuslt;
  }

  static Node findLastNonEmptyPathNode(final Path path) {
    Node found = null;
    final List<Node> list = newArrayList(path.iterator());
    Collections.reverse(list);
    for (final Node node : list) {
      if (!isEmpty(node.getName())) {
        found = node;
        break;
      }
    }
    return found;
  }

  @Override
  public Optional<List<ValidationError>> validationErrors( //
    final ConstraintViolationException exception //
  ) {
    final List<ValidationError> validationErrors = newLinkedList();

    for (final ConstraintViolation<?> violation : exception.getConstraintViolations()) {
      final Node pathNode = findLastNonEmptyPathNode(violation.getPropertyPath());

      // path is probably useful only for properties (fields)
      if (pathNode != null && pathNode.getKind() == ElementKind.PROPERTY) {
        validationErrors.add( //
          ValidationError.validationErrorBuilder() //
            .field(pathNode.getName()) //
            .rejected(convertToString(this.conversionService, violation.getInvalidValue())) //
            .message(violation.getMessage()) //
            .build() //
        );
        // type level constraints etc.
      } else {
        validationErrors.add( //
          ValidationError.validationErrorBuilder() //
            .message(violation.getMessage()) //
            .build() //
        );
      }
    }

    return Optional.of(validationErrors);
  }
}
