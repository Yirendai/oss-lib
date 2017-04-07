package com.yirendai.oss.lib.security.api;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.yirendai.oss.boot.autoconfigure.AppProperties.getEnvironment;
import static com.yirendai.oss.boot.autoconfigure.AppProperties.getProdEnvironment;
import static com.yirendai.oss.boot.autoconfigure.AppSecurityProperties.ENCRYPTED_FIELD_PREFIX;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.yirendai.oss.lib.common.crypto.EncodeCipher;
import com.yirendai.oss.lib.security.internal.BaseGrantedAuthority;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

/**
 * Created by zhanghaolun on 16/7/6.
 */
@Slf4j
public abstract class BaseUserDetailsAuthenticationProvider<U extends User>
  extends org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
  implements AuthenticationProvider, UserDetailsService, ApplicationListener<ContextRefreshedEvent> {

  @Getter
  private EncodeCipher cipher;
  @Autowired(required = false)
  @Setter
  @Getter
  private PasswordEncoder passwordEncoder;
  @Autowired(required = false)
  private SecurityProperties securityProperties;

  @Override
  protected final void additionalAuthenticationChecks( //
    final org.springframework.security.core.userdetails.UserDetails userDetails, //
    final UsernamePasswordAuthenticationToken authentication //
  ) throws AuthenticationException {
    // no-op
  }

  @Override
  public final org.springframework.security.core.userdetails.UserDetails loadUserByUsername( //
    final String username //
  ) throws UsernameNotFoundException {
    final GenericUser found = this.findEverywhere(username);
    if (found != null) {
      return found;
    } else {
      throw new UsernameNotFoundException(username);
    }
  }

  @Override
  protected final org.springframework.security.core.userdetails.UserDetails retrieveUser( //
    final String username, //
    final UsernamePasswordAuthenticationToken authentication //
  ) throws AuthenticationException {
    if (log.isDebugEnabled()) {
      log.debug("retrieveUser: {}", username);
    }

    final String passwordInput = (String) authentication.getCredentials();
    final String password = decryptIfEncrypted(passwordInput);
    if (isBlank(password)) {
      log.warn("Username {}: no password provided", username);
      throw new BadCredentialsException("Please enter password");
    }

    final GenericUser user = this.findEverywhere(username);
    if (user == null) {
      log.warn("Username {} password {}: user not found", username, password);
      throw new UsernameNotFoundException(username);
    }

    final PasswordEncoder passwordEncoder = this.getPasswordEncoder();
    final Boolean passwordMatches;
    if (passwordEncoder != null) {
      passwordMatches = passwordEncoder.matches(password, user.getPassword());
    } else {
      passwordMatches = org.apache.commons.lang3.StringUtils.equals(password, user.getPassword());
    }
    if (!passwordMatches) {
      log.warn("Username {} password {}: invalid password", username, password);
      throw new BadCredentialsException("Invalid Login");
    }

    if (!user.isEnabled()) {
      log.warn("Username {}: disabled", username);
      throw new BadCredentialsException("User disabled");
    }

    return user;
  }

  public final GenericUser findEverywhere(final String username) {
    final User user = this.findByName(username);

    final GenericUser result;
    if (user == null) {
      result = this.findSecurityUser(username);
    } else {
      result = GenericUser.fromUser(user);
    }
    return result;
  }

  protected final GenericUser findSecurityUser(final String username) {
    final GenericUser result;
    if (this.securityProperties == null) {
      result = null;
    } else {
      final SecurityProperties.User securityUser = this.securityProperties.getUser();
      if (securityUser != null && isNotBlank(securityUser.getName()) && securityUser.getName().equals(username)) {
        final Set<GrantedAuthority> authorities = securityUser.getRole() //
          .stream() //
          .map(role -> new BaseGrantedAuthority(Security.ROLE_PREFIX + role)) //
          .collect(toSet());
        final PasswordEncoder passwordEncoder = this.getPasswordEncoder();
        final String password = passwordEncoder != null ? //
          passwordEncoder.encode(securityUser.getPassword()) : securityUser.getPassword();

        final UserDetails userDetails = UserDetails.userDetailsBuilder() //
          .authorities(authorities) //
          .enabled(true) //
          .id("") //
          .name(securityUser.getName()) //
          .password(password) //
          .properties(ImmutableMap.of()) //
          .build();
        result = GenericUser.fromUser(userDetails);
      } else {
        result = null;
      }
    }
    return result;
  }

  // ------------------------------ it users ------------------------------

  String decryptIfEncrypted(final String text) {
    final String result;
    if (text == null) {
      result = null;
    } else if (text.startsWith(ENCRYPTED_FIELD_PREFIX)) {
      checkState(this.cipher != null, "Encrypt not supported.");
      result = this.cipher.decrypt(text.substring(ENCRYPTED_FIELD_PREFIX.length()));
    } else {
      result = text;
    }
    return result;
  }

  @Override
  public void onApplicationEvent(final ContextRefreshedEvent event) {
    final Boolean prodEnvironment = getProdEnvironment(getEnvironment(event.getApplicationContext().getEnvironment()));
    if (!prodEnvironment) {
      log.info("init test users on non-production environment.");
      this.initTestUsers();
    } else {
      log.info("skip init test users on production environment.");
    }
  }

  protected abstract List<U> testUsers();

  public final List<U> initTestUsers() {
    final List<U> users = this.testUsers();
    final List<U> result = newArrayListWithExpectedSize(users.size());
    for (final U user : users) {
      final U found = this.findByName(user.getName());
      if (found == null) {
        final Set<GrantedAuthority> authorities = user.getAuthorities() != null ? //
          user.getAuthorities() : //
          ImmutableSet.of();
        authorities.forEach(this::saveRole);
        result.add(this.save(user));
      } else if (!found.isEnabled() || !found.equals(user)) {
        this.delete(found);
        result.add(this.save(user));
      } else {
        result.add(found);
      }
    }
    return result;
  }

  public List<U> deleteTestUsers() {
    final List<U> users = this.testUsers();
    final List<U> result = newArrayListWithExpectedSize(users.size());
    for (final U user : users) {
      final U found = this.findByName(user.getName());
      if (found != null) {
        this.delete(found);
      }
    }
    return result;
  }

  public final void setCipher(final EncodeCipher cipher) {
    this.cipher = cipher;
  }

  protected abstract U findByName(String username);

  protected abstract U save(U user);

  protected abstract void delete(U user);

  protected abstract GrantedAuthority saveRole(GrantedAuthority authority);
}
