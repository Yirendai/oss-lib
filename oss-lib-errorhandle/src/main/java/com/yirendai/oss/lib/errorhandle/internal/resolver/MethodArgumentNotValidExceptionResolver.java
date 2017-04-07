package com.yirendai.oss.lib.errorhandle.internal.resolver;

import static com.google.common.collect.Lists.newLinkedList;

import com.yirendai.oss.lib.errorhandle.api.AbstractConcreteExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ValidationError;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Optional;

public class MethodArgumentNotValidExceptionResolver //
  extends AbstractConcreteExceptionResolver<MethodArgumentNotValidException> {

  @Override
  public Optional<List<ValidationError>> validationErrors(final MethodArgumentNotValidException exception) {
    final List<ValidationError> validationErrors = newLinkedList();

    final BindingResult bindingResult = exception.getBindingResult();
    for (final ObjectError err : bindingResult.getGlobalErrors()) {
      validationErrors.add(ValidationError.validationErrorBuilder() //
        .message(err.getDefaultMessage()) //
        .build() //
      );
    }
    for (final FieldError err : bindingResult.getFieldErrors()) {
      final Object rejectedValue = err.getRejectedValue();
      final String rejected = rejectedValue != null ? rejectedValue.toString() : null;
      validationErrors.add(ValidationError.validationErrorBuilder() //
        .field(err.getField()) //
        .rejected(rejected) //
        .message(err.getDefaultMessage()) //
        .build() //
      );
    }

    return Optional.of(validationErrors);
  }
}
