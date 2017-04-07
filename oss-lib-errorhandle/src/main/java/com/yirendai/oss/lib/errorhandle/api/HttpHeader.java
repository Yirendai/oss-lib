package com.yirendai.oss.lib.errorhandle.api;

import static com.google.common.collect.Lists.newArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import org.springframework.http.HttpHeaders;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by zhanghaolun on 16/8/12.
 */
@XmlRootElement(name = "header")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class HttpHeader implements Serializable {

  @JsonProperty("name")
  @XmlElement(name = "name")
  private String name;
  @JsonProperty("values")
  @XmlElementWrapper(name = "values")
  @XmlElement(name = "value")
  private String[] values;

  public static HttpHeaders toHttpHeaders(final HttpHeader... headers) {
    final HttpHeaders result;
    if (headers == null) {
      result = null;
    } else {
      result = new HttpHeaders();
      for (final HttpHeader header : headers) {
        result.put(header.getName(), newArrayList(header.getValues()));
      }
    }
    return result;
  }

  public static HttpHeader[] fromHttpHeaders(final HttpHeaders headers) {
    final HttpHeader[] result;
    if (headers == null) {
      result = null;
    } else {
      result = headers.entrySet().stream() //
        .map(entry -> {
          // final String name = entry.getKey();
          final List<String> value = entry.getValue();
          final HttpHeader header = new HttpHeader();
          header.setName(entry.getKey());
          header.setValues(value != null ? value.toArray(new String[value.size()]) : new String[]{});
          return header;
        }) //
        .toArray(size -> new HttpHeader[size]);
    }
    return result;
  }

  public String getName() {
    return this.name;
  }

  public String[] getValues() {
    return this.values != null ? Arrays.copyOf(this.values, this.values.length) : null;
  }
}
