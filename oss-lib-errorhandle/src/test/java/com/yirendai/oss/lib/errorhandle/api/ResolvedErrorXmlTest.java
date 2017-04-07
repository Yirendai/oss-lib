package com.yirendai.oss.lib.errorhandle.api;


import static com.yirendai.oss.lib.errorhandle.api.ResolvedErrorTestScenario.resolvedErrorScenario;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.jayway.restassured.path.xml.XmlPath;
import com.yirendai.oss.lib.common.Jackson2Utils;
import com.yirendai.oss.lib.common.JaxbUtils;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


@Slf4j
public class ResolvedErrorXmlTest {

  private XmlMapper xmlMapper;
  private Jaxb2Marshaller jaxb2Marshaller;

  @Before
  public void setUp() {
    this.xmlMapper = Jackson2Utils.setupObjectMapper(null, new XmlMapper());

    this.jaxb2Marshaller = JaxbUtils.jaxb2Marshaller();
    this.jaxb2Marshaller.setClassesToBeBound(ResolvedError.class);
  }

  @Test
  public void testSerializeXmlMapper() throws IOException, JAXBException {
    final String actual = this.xmlMapper.writeValueAsString(resolvedErrorScenario());

    assertSerialize(actual);
  }

  @Test
  public void testSerializeJaxb2() throws IOException {
    final StringWriter actual = new StringWriter();
    this.jaxb2Marshaller.marshal(resolvedErrorScenario(), new StreamResult(actual));

    assertSerialize(actual.toString());
  }

  @Test
  public void testSerializeJAXB() throws IOException {
    final StringWriter actual = new StringWriter();
    JAXB.marshal(resolvedErrorScenario(), new StreamResult(actual));

    assertSerialize(actual.toString());
  }

  @Test
  public void testDeserializeXmlMapper() throws JAXBException, IOException {
    final ResolvedError actual = this.xmlMapper.readValue(getStandardError(), ResolvedError.class);

    assertEquals(resolvedErrorScenario(), actual);
  }

  @Test
  public void testDeserializeJaxb2() throws IOException {
    final Object actual = this.jaxb2Marshaller.unmarshal(new StreamSource(getStandardError()));

    assertEquals(resolvedErrorScenario(), actual);
  }

  @Test
  public void testDeserializeJAXB() throws IOException {
    final ResolvedError actual = JAXB.unmarshal(getStandardError(), ResolvedError.class);

    assertEquals(resolvedErrorScenario(), actual);
  }

  private void assertSerialize(final String actual) throws FileNotFoundException {
    log.info("xml actual: {}", actual);
    final XmlPath xmlPath = new XmlPath(actual);
    final ResolvedError error = resolvedErrorScenario();

    assertEquals(error.getError(), xmlPath.getString("error.error"));
    assertEquals(error.getMessage(), xmlPath.getString("error.message"));
    assertEquals(error.getTimestamp(), (Long) xmlPath.getLong("error.timestamp"));
    assertEquals(error.getTrace(), xmlPath.getString("error.trace"));
    assertEquals(error.getHeaders()[0].getName(), xmlPath.getString("error.headers.header[0].name"));
    assertEquals(error.getHeaders()[0].getValues()[0],
      xmlPath.getString("error.headers.header[0].values.value[0]"));
    assertEquals(error.getLocalizedMessage(), xmlPath.getString("error.localizedMessage"));
    assertEquals(error.getTracks()[0], xmlPath.getString("error.tracks.track[0]"));
    assertEquals(error.getTracks()[1], xmlPath.getString("error.tracks.track[1]"));
  }

  private File getStandardError() throws IOException {
    return ResourceUtils.getFile("classpath:resolvedErrorScenario.xml");
  }
}
