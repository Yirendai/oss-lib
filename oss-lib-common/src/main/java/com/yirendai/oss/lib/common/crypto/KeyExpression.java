package com.yirendai.oss.lib.common.crypto;

import static com.yirendai.oss.lib.common.crypto.CryptoConstants.COLON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by zhanghaolun on 16/11/16.
 */
@XmlRootElement(name = "key") // Jaxb2RootElementHttpMessageConverter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_EMPTY) // for Jackson 2.x
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY) // for Jackson 1.x
@EqualsAndHashCode
@NoArgsConstructor
@Setter
@Getter
@Slf4j
public class KeyExpression {

  private static final String KEY_SPEC_NOT_PRESENT = "key spec not present, use our keygen to generate valid keys";

  private String spec;
  private String value;

  KeyExpression(final String spec, final String value) {
    if (isBlank(spec)) {
      log.error(KEY_SPEC_NOT_PRESENT);
      throw new IllegalArgumentException(KEY_SPEC_NOT_PRESENT);
    }
    this.spec = spec;
    this.value = value;
  }

  public KeyExpression(final String expression) {
    if (isNotBlank(expression)) {
      final int index = expression.indexOf(COLON);
      if (index <= 0) {
        log.error(KEY_SPEC_NOT_PRESENT);
        throw new IllegalArgumentException(KEY_SPEC_NOT_PRESENT);
      }
      this.spec = expression.substring(0, index);
      this.value = expression.substring(index + 1);
    }
  }

  @Override
  public String toString() {
    return "" + this.spec + COLON + this.value;
  }

  public Boolean isPresent() {
    return isNotBlank(this.spec) && isNotBlank(this.value);
  }
}
