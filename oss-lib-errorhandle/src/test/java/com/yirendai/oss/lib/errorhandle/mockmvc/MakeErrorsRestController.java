package com.yirendai.oss.lib.errorhandle.mockmvc;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MakeErrorsRestController {

  @ResponseStatus(HttpStatus.NOT_FOUND)
  static class NotFoundException extends RuntimeException {

    NotFoundException(final String string) {
      super(string);
    }
  }

  public String getName() {
    return "meifans";
  }

  @RequestMapping(value = "/nest", method = RequestMethod.GET)
  public String nestException() throws NotFoundException {
    throw new NotFoundException("go");
  }
}
