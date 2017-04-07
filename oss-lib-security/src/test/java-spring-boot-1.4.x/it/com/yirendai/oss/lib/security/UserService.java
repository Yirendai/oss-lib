package it.com.yirendai.oss.lib.security;

import static com.yirendai.oss.lib.security.api.Security.ROLE_PREFIX;
import static com.yirendai.oss.lib.security.api.StaticPrivilege.PRIVILEGE_PREFIX;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserService extends BaseUserDetailsAuthenticationProvider<User> {

  @Autowired(required = false)
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserRoleRepository userRoleRepository;

  @Override
  protected void delete(final User user) {
    if (user != null) {
      this.userRepository.delete(user.getUserId());
    }
  }

  public User findById(final Long id) {
    return this.userRepository.findOne(id);
  }

  @Override
  public User findByName(final String name) {
    return this.userRepository.findByName(name);
  }

  @Override
  protected User save(final User user) {
    final User result;
    if (user != null) {
      user.passwordEncode(this.passwordEncoder);
      result = this.userRepository.save(user);
    } else {
      result = null;
    }
    return result;
  }

  @Override
  protected GrantedAuthority saveRole(final GrantedAuthority authority) {
    final GrantedAuthority result;
    if (authority == null || authority.getAuthority() == null
      || authority.getAuthority().startsWith(PRIVILEGE_PREFIX)) {
      result = authority;
    } else {
      final String name = authority.getAuthority().replaceFirst(ROLE_PREFIX, "");
      final UserRole found = this.userRoleRepository.findOne(name);
      if (found == null) {
        result = this.userRoleRepository.save(new UserRole(name, ImmutableList.of()));
      } else {
        result = found;
      }
    }
    return result;
  }

  @Override
  protected List<User> testUsers() {
    return ImmutableList.of(
      User.userBuilder().email("user@somedomain.com").enabled(true) //
        .name("test_user").password("user_pass") //
        .roles(ImmutableSet.of(UserRole.ROLE_USER_AUTHORITY)) //
        .build(),
      User.userBuilder().email("admin@somedomain.com").enabled(true) //
        .name("test_admin").password("admin_pass") //
        .roles(ImmutableSet.of(UserRole.ROLE_ADMIN_AUTHORITY)) //
        .build());
  }
}
