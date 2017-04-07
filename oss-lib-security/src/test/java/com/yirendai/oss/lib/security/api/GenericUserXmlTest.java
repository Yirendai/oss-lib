package com.yirendai.oss.lib.security.api;


import static com.yirendai.oss.lib.common.Defaults.ISO8601;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXB;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Created by Meifans on 17/1/12.
 */
@Slf4j
public class GenericUserXmlTest {

  private XmlMapper xmlMapper;
  private Jaxb2Marshaller jaxb2;

  @Before
  public void setup() {
    this.xmlMapper = Jackson2Utils.setupObjectMapper(null, new XmlMapper());
    this.jaxb2 = JaxbUtils.jaxb2Marshaller();
    this.jaxb2.setClassesToBeBound(GenericUser.class);
  }

  @Test
  public void testSerializeXmlMapper() throws JsonProcessingException {
    final String actual = this.xmlMapper.writeValueAsString(GenericUserTestScenario.genericUserScenario());

    assertSerialize(actual);
  }

  @Test
  public void testSerializeJaxb2() {
    final StringWriter actual = new StringWriter();
    this.jaxb2.marshal(GenericUserTestScenario.genericUserScenario(), new StreamResult(actual));

    assertSerialize(actual.toString());
  }

  @Test
  public void testSerializeJAXB() {
    final StringWriter actual = new StringWriter();
    JAXB.marshal(GenericUserTestScenario.genericUserScenario(), new StreamResult(actual));

    assertSerialize(actual.toString());
  }

  @Test
  public void testDeserializeXmlMapper() throws IOException {
    final GenericUser actual = this.xmlMapper.readValue(getStandardUser(), GenericUser.class);

    assertEquals(GenericUserTestScenario.genericUserScenario(), actual);
  }

  @Test
  public void testDeserializeJaxb2() throws IOException {
    final Object actual = this.jaxb2.unmarshal(new StreamSource(getStandardUser()));

    assertEquals(GenericUserTestScenario.genericUserScenario(), actual);
  }

  @Test
  public void testDeserializeJAXB() throws IOException {
    final GenericUser actual = JAXB.unmarshal(getStandardUser(), GenericUser.class);

    assertEquals(GenericUserTestScenario.genericUserScenario(), actual);
  }

  private File getStandardUser() throws IOException {
    return ResourceUtils.getFile("classpath:genericUserScenario.xml");
  }

  private void assertSerialize(String actual) {
    final XmlPath xmlPath = new XmlPath(actual);
    final GenericUser scenario = GenericUserTestScenario.genericUserScenario();

    assertEquals(scenario.getProperties().get("tag"), xmlPath.getString("genericUser.properties.tag"));
    assertEquals(ISO8601.print(scenario.getTimestamp()), xmlPath.getString("genericUser.timestamp"));
    assertEquals(scenario.getUuid(), xmlPath.getString("genericUser.uuid"));
    assertEquals(scenario.getAuthorities().iterator().next().getAuthority(),
      xmlPath.getString("genericUser.authorities.authority"));
    assertEquals(scenario.getUsername(), xmlPath.getString("genericUser.username"));
    assertEquals(scenario.getPassword(), xmlPath.getString("genericUser.password"));
  }
}
