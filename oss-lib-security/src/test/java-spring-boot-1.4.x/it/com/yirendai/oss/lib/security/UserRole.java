package it.com.yirendai.oss.lib.security;

import static lombok.AccessLevel.PROTECTED;

import com.google.common.collect.ImmutableList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yirendai.oss.lib.security.api.AbstractRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * Created by zhanghaolun on 16/10/28.
 */
@Entity
@Builder(builderMethodName = "userRoleBuilder")
@AllArgsConstructor
@NoArgsConstructor
@Setter(PROTECTED)
@Getter
public class UserRole extends AbstractRole {

  public static final String ROLE_ADMIN = "ADMIN";
  public static final UserRole ROLE_ADMIN_AUTHORITY = new UserRole(ROLE_ADMIN, ImmutableList.of());

  public static final String ROLE_USER = "USER";
  public static final UserRole ROLE_USER_AUTHORITY = new UserRole(ROLE_USER, ImmutableList.of());

  @Id
  @Column(nullable = false, length = 36)
  private String name;

  @JsonIgnore
  @ManyToMany(targetEntity = User.class, mappedBy = "roles", fetch = FetchType.LAZY)
  private List<User> users = new LinkedList<>();
}
