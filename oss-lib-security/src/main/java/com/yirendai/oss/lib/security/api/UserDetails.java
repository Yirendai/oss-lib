package com.yirendai.oss.lib.security.api;

import static lombok.AccessLevel.PROTECTED;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO 考虑XML序列化, 尤其是JAXB针对Map.
 * Default in memory user data impl.
 * Created by zhanghaolun on 16/10/30.
 */
@Builder(builderMethodName = "userDetailsBuilder")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Setter(PROTECTED)
@Getter
@ApiModel
public class UserDetails extends AbstractUser {

  @ApiModelProperty(dataType = "java.lang.String", example = "ADMIN,USER,OTHER")
  private Set<GrantedAuthority> authorities = new HashSet<>();

  private boolean enabled;

  private String id;

  private String name;

  private String password;

  private Map<String, String> properties;
}
