package it.com.yirendai.oss.lib.security;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yirendai.oss.lib.security.api.AbstractUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

/**
 * Created by zhanghaolun on 16/10/28.
 */
@Entity
@Builder(builderMethodName = "userBuilder")
@AllArgsConstructor
@NoArgsConstructor
@Setter(PROTECTED)
@Getter
public class User extends AbstractUser {

  private static final long serialVersionUID = 1L;

  // build in properties

  @Type(type = "yes_no")
  private boolean enabled;

  @Column(nullable = false, length = 80)
  @NonNull
  private String name;

  @Column(nullable = false, length = 80)
  @NonNull
  private String password;

  @NonNull
  @ManyToMany(targetEntity = UserRole.class, fetch = FetchType.EAGER)
  private Set<UserRole> roles = new HashSet<>();

  @Id
  @GeneratedValue
  private Long userId;

  // extended properties
  @Column(nullable = false, length = 80)
  @NonNull
  private String email;

  public void addRole(final UserRole role) {
    final Set<UserRole> roles = newLinkedHashSet(this.roles);
    roles.add(role);
    this.roles = ImmutableSet.copyOf(roles);
  }

  @Override
  public Set<GrantedAuthority> getAuthorities() {
    return UserRolePrivileges.getAuthorities(this.getRoles().stream().map(UserRole::getName).collect(toSet()));
  }

  @Override
  public String getId() {
    return this.userId != null ? this.userId.toString() : null;
  }

  @JsonIgnore
  @Override
  public Map<String, String> getProperties() {
    return ImmutableMap.of("email", this.getEmail());
  }

  public void passwordEncode(final PasswordEncoder passwordEncoder) {
    if (passwordEncoder != null && isNotBlank(this.password)) {
      this.password = passwordEncoder.encode(this.password);
    }
  }
}
