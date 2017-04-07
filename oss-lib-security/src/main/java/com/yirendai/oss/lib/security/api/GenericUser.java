package com.yirendai.oss.lib.security.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.yirendai.oss.lib.common.CodecUtils.urlDecode;
import static com.yirendai.oss.lib.common.CodecUtils.urlEncode;
import static com.yirendai.oss.lib.security.api.OAuth2Utils.fromOAuth2Authentication;
import static com.yirendai.oss.lib.security.api.OAuth2Utils.isOAuth2Authentication;
import static java.lang.Boolean.FALSE;
import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.splitPreserveAllTokens;

import com.google.common.collect.ImmutableSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yirendai.oss.lib.common.Defaults;
import com.yirendai.oss.lib.common.JaxbMapAdapter;
import com.yirendai.oss.lib.common.JaxbUtils;
import com.yirendai.oss.lib.security.internal.BaseGrantedAuthority;

import io.swagger.annotations.ApiModelProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.ClassUtils;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A generic token of user.
 * Never extends GenericUser, use {@link GenericUser#fromPrincipal} {@link GenericUser#fromUser}.
 * username =&gt; type:id:name:tel:wx
 */
@XmlRootElement(name = "genericUser")
@XmlAccessorType(XmlAccessType.FIELD)
@Builder(builderMethodName = "genericUserBuilder")
@AllArgsConstructor(access = PACKAGE)
@NoArgsConstructor(access = PRIVATE)
@EqualsAndHashCode(exclude = {"timestamp", "uuid"})
@ToString
@Setter(value = PRIVATE)
@Getter
@Slf4j
@SuppressWarnings("serial")
public final class GenericUser //
  implements org.springframework.security.core.userdetails.UserDetails, CredentialsContainer {

  public static final String GENERIC_USER_COOKIE = "genericUserCookie";
  public static final String GENERIC_USER_TOKEN = "genericUserToken";
  static final String DELIMITER = "+";
  static final String USER_TYPE_UNKNOWN = "UNKNOWN";
  private static final String CLASS_OAUTH2_AUTHENTICATION = //
    "org.springframework.security.oauth2.provider.OAuth2Authentication";
  private static final Boolean OAUTH2_AUTHENTICATION_PRESENT;

  static {
    OAUTH2_AUTHENTICATION_PRESENT = ClassUtils.isPresent( //
      CLASS_OAUTH2_AUTHENTICATION, currentThread().getContextClassLoader());
  }

  private boolean accountNonExpired;
  private boolean accountNonLocked;
  @ApiModelProperty(dataType = "java.lang.String", example = "ADMIN,USER,OTHER")
  @XmlElementWrapper(name = "authorities")
  @XmlElement(name = "authority", type = BaseGrantedAuthority.class)
  private Set<GrantedAuthority> authorities;
  private boolean credentialsNonExpired;
  private boolean enabled;
  private String password;
  private String username;

  @XmlJavaTypeAdapter(value = JaxbMapAdapter.class, type = Map.class)
  private Map<String, String> properties;
  @ApiModelProperty(hidden = true)
  @NonNull
  @XmlJavaTypeAdapter(value = JaxbUtils.DatimeAdapter.class, type = DateTime.class)
  private DateTime timestamp;
  @NonNull
  private String uuid;

  public static GenericUser fromPrincipal(final Principal principal) {
    final GenericUser result;
    if (principal == null) {
      result = null;
    } else {
      if (principal instanceof PreAuthenticatedAuthenticationToken) {
        final PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) principal;
        result = isGenericUser(token.getPrincipal()) ? (GenericUser) token.getPrincipal() : null;
      } else if (principal instanceof UsernamePasswordAuthenticationToken) {
        final UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        result = isGenericUser(token.getPrincipal()) ? (GenericUser) token.getPrincipal() : null;
      } else if (OAUTH2_AUTHENTICATION_PRESENT && isOAuth2Authentication(principal)) {
        result = fromOAuth2Authentication(principal);
      } else if (principal instanceof Authentication) {
        final Authentication authentication = (Authentication) principal;
        result = isGenericUser(authentication.getPrincipal()) ? (GenericUser) authentication.getPrincipal() : null;
      } else {
        if (log.isInfoEnabled()) {
          log.info("unknown principal: {}:{}", principal.getClass(), principal);
        }
        result = null;
      }
    }
    return result;
  }

  public static Optional<GenericUser> fromSecurityContext() {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication authentication = securityContext.getAuthentication();
    return Optional.ofNullable(fromPrincipal(authentication));
  }

  public static GenericUser fromUser(final User user) {
    return user != null ? fromUser(user, uuid()) : null;
  }

  private static GenericUser fromUser(final User user, final String uuid) {
    final GenericUser output;
    if (user == null) {
      output = null;
    } else {
      final Set<GrantedAuthority> authorities = user.getAuthorities() != null ? //
        user.getAuthorities().stream() //
          .map(authority -> new BaseGrantedAuthority(authority.getAuthority())) //
          .collect(toSet()) : ImmutableSet.of();
      output = new GenericUser();
      output.setAccountNonExpired(user.isAccountNonExpired());
      output.setAccountNonLocked(user.isAccountNonLocked());
      output.setAuthorities(authorities);
      output.setCredentialsNonExpired(user.isCredentialsNonExpired());
      output.setEnabled(user.isEnabled());
      // DaoAuthenticationProvider#additionalAuthenticationChecks need this value
      output.setPassword(user.getPassword());
      output.setUsername(toUsername(user));
      output.setProperties(user.getProperties());

      output.setTimestamp(Defaults.now());
      output.setUuid(uuid);
    }
    return output;
  }

  public static boolean isGenericUser(final Object object) {
    return object != null && GenericUser.class.isAssignableFrom(object.getClass());
  }

  public static boolean isGenericUserLogin(final GenericUser genericUser) {
    return genericUser != null && isNotBlank(genericUser.getId()) && !USER_TYPE_UNKNOWN.equals(genericUser.getType());
  }

  private static String toUsername(final User user) {
    final String type = checkNotNull(user, "null user").getType();
    final String id = user.getId() != null ? user.getId() : "";
    final String name = user.getName();
    return toUsername(type, id, name);
  }

  static String toUsername(final String type, final String id, final String name) {
    return urlEncode(type) + DELIMITER //
      + urlEncode(id) + DELIMITER //
      + urlEncode(isNotBlank(name) ? name : "");
  }

  public static GenericUser unknownUser() {
    return GenericUser.genericUserBuilder() //
      .authorities(ImmutableSet.of()) //
      .enabled(FALSE) //
      .username(GenericUser.toUsername(GenericUser.USER_TYPE_UNKNOWN, "", GenericUser.USER_TYPE_UNKNOWN)) //
      .password("") //
      .accountNonExpired(FALSE) //
      .accountNonLocked(FALSE) //
      .credentialsNonExpired(FALSE) //
      .timestamp(Defaults.now()) //
      .uuid(GenericUser.uuid()) //
      .build();
  }

  private static String uuid() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }

  @Override
  public void eraseCredentials() {
    this.password = null;
  }

  @JsonIgnore
  public String getType() {
    return this.fromUsername(0, null);
  }

  // @JsonIgnore//api needs expose id
  public String getId() {
    return this.fromUsername(1, "");
  }

  @JsonIgnore
  public String getName() {
    return this.fromUsername(2, "");
  }

  private String fromUsername(final int index, final String defaultValue) {
    return isNotBlank(this.username) ? urlDecode(splitPreserveAllTokens(this.username, DELIMITER)[index]) :
      defaultValue;
  }

  private void setUuid(final String uuid) {
    checkArgument(isNotBlank(uuid));
    this.uuid = uuid.replaceAll("-", "");
  }

  public UserDetails toUserInfo() {
    return UserDetails.userDetailsBuilder()
      .authorities(this.getAuthorities() != null ? this.getAuthorities() : ImmutableSet.of())
      .enabled(this.isEnabled())
      .id(this.getId())
      .name(this.getName())
      .password("[PROTECTED]")
      .properties(this.getProperties())
      .build();
  }
}
