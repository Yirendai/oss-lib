package com.yirendai.oss.lib.common;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import org.joda.time.DateTime;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.StringReader;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.stream.StreamSource;


public final class JaxbUtils {

  private JaxbUtils() {
  }

  /**
   * jaxb2 marshaller.
   *
   * @return jaxb2 marshaller
   */
  public static Jaxb2Marshaller jaxb2Marshaller() {
    final Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    // marshaller.setPackagesToScan("");
    final Map<String, Object> properties = newLinkedHashMap();
    properties.put(Marshaller.JAXB_ENCODING, Defaults.UTF_8.name());
    properties.put(Marshaller.JAXB_FORMATTED_OUTPUT, FALSE);
    properties.put(Marshaller.JAXB_FRAGMENT, TRUE);
    marshaller.setMarshallerProperties(properties);
    return marshaller;
  }

  /**
   * unmarshal.
   *
   * @param marshaller marshaller
   * @param xml        xml
   * @param type       type
   * @param <T>        type
   * @return object
   */
  @SneakyThrows
  public static <T> T unmarshal(final Jaxb2Marshaller marshaller, final String xml,
    final Class<T> type) {
    final JAXBElement<T> element = marshaller.getJaxbContext().createUnmarshaller()
      .unmarshal(new StreamSource(new StringReader(xml)), type);
    return element.getValue();
  }

  @NoArgsConstructor
  public static class DatimeAdapter extends XmlAdapter<String, DateTime> {

    @Override
    public DateTime unmarshal(String s) throws Exception {
      return new DateTime(s, Defaults.UTC_P8);
    }

    @Override
    public String marshal(DateTime date) throws Exception {
      return Defaults.ISO8601.print(date);
    }
  }
}
