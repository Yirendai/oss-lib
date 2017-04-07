package com.yirendai.oss.lib.errorhandle.api;

import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by zhanghaolun on 16/7/3.
 */
@XmlRootElement(name = "validationError")
@XmlAccessorType(XmlAccessType.FIELD)
// @com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement(localName = "error")
@JsonInclude(JsonInclude.Include.NON_EMPTY) // for Jackson 2.x
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY) // for Jackson 1.x
@Builder(builderMethodName = "validationErrorBuilder")
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor(access = PRIVATE)
@EqualsAndHashCode
@ToString
@Setter(PRIVATE)
@Getter
public class ValidationError implements Serializable {
  @XmlElement
  private String field;
  @XmlElement
  private String rejected;
  @XmlElement
  private String message;
}
