package com.yirendai.oss.lib.common;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbMapWrapper {
  @XmlAnyElement
  private List<Object> elements = new ArrayList<>();
}
