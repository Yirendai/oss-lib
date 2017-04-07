package com.yirendai.oss.lib.security.swagger;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;

import springfox.documentation.builders.ApiListingBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.AllowableListValues;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.Operation;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingBuilderPlugin;
import springfox.documentation.spi.service.contexts.ApiListingContext;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by zhanghaolun on 16/10/31.
 */
@Deprecated
@Order(value = SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class SecurityApiListingPlugin implements ApiListingBuilderPlugin { //
  // ApiListingScannerPlugin not working, debug at DocumentationPluginsManager#additionalListings

  //  @Override
  public List<ApiDescription> apply(final DocumentationContext context) {
    return this.additionalOperations();
  }

  //  @Override
  public void apply(final ApiListingContext apiListingContext) {
    //ApiListingBuilderPlugin, this will override all api
    //  apiListingContext.apiListingBuilder() //
    //  .apis(this.additionalOperations());

    final Class<?> controllerClass = apiListingContext.getResourceGroup().getControllerClass();
    if (controllerClass != null && !controllerClass.getName().startsWith("org.springframework.boot.actuate")) {
      final ApiListingBuilder builder = apiListingContext.apiListingBuilder();
      final Field field = ReflectionUtils.findField(builder.getClass(), "apis");
      ReflectionUtils.makeAccessible(field);
      final Object value = Optional.ofNullable(ReflectionUtils.getField(field, builder)) //
        .orElse(Collections.emptyList());
      @SuppressWarnings("unchecked")
      final List<ApiDescription> apis = (List<ApiDescription>) value;

      final List<ApiDescription> newApis = new ArrayList<>(apis);
      newApis.addAll(this.additionalOperations());

      builder.apis(newApis);
    }
  }

  @Override
  public boolean supports(final DocumentationType documentationType) {
    return DocumentationType.SWAGGER_2.equals(documentationType);
  }

  private List<ApiDescription> additionalOperations() {
    return Lists.newArrayList(
      new ApiDescription( // apiDescription
        "/oauth/token", // path
        "security endpoints", // description, not showed
        Lists.newArrayList( // operations
          new Operation( // operation
            HttpMethod.POST, // method
            "Retrieve Access Token", // summary
            "", // notes
            null, // responseModel
            "oauth-token", // uniqueId
            0, // position
            Sets.newHashSet("oauth2-filter"), // tags
            newHashSet(MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE), // produces
            newHashSet(MediaType.APPLICATION_FORM_URLENCODED_VALUE), // consumes
            Collections.emptySet(), // protocol
            newArrayList(), // securityReferences
            Lists.newArrayList( // parameters
              new Parameter( // parameter
                "grant_type", // name
                "Grant type", // description
                "client_credentials", // defaultValue
                true, // required
                false, // allowMultiple
                new ModelRef("java.lang.String"), // modelRef
                null, // type
                new AllowableListValues(newArrayList("client_credentials", "authorize_code"), "String"),
                // allowableValues
                "form", // paramType
                "", // paramAccess
                false, // hidden
                newArrayList() // vendorExtensions
              )
            ),
            Sets.newHashSet( // responseMessages
              new ResponseMessage( // responseMessage
                HttpStatus.OK.value(), // code
                HttpStatus.OK.getReasonPhrase(), // message
                new ModelRef("org.springframework.security.oauth2.common.DefaultOAuth2AccessToken"), // responseModel
                Collections.emptyMap(), // headers
                newArrayList() // vendorExtensions
              )
            ),
            null, // deprecated
            false, // isHidden
            newArrayList() // vendorExtensions
          )
        ),
        false // hidden
      )
    );
  }
}
