package com.yirendai.oss.lib.common;

import com.sun.org.apache.xerces.internal.dom.TextImpl;

import lombok.NoArgsConstructor;

import org.w3c.dom.Node;

import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

@NoArgsConstructor
public class JaxbMapAdapter extends XmlAdapter<JaxbMapWrapper, Map<String, String>> {

  @Override
  public Map<String, String> unmarshal(final JaxbMapWrapper wrapper) throws Exception {
    return wrapper.getElements().stream().collect( //
      Collectors.toMap( //
        k -> {
          Node node = (Node) k;
          return node.getLocalName();
        },  //
        v -> {
          Node node = (Node) v;
          TextImpl child = (TextImpl) node.getFirstChild();
          return child.getData();
        }));
  }

  @Override
  public JaxbMapWrapper marshal(final Map<String, String> map) throws Exception {
    final JaxbMapWrapper wrapper = new JaxbMapWrapper();
    map.forEach((key, value) -> //
      wrapper.getElements().add(new JAXBElement<>(new QName(key), String.class, value)));

    return wrapper;
  }
}
