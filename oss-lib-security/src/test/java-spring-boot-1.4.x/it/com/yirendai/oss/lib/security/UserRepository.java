package it.com.yirendai.oss.lib.security;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhanghaolun on 16/6/23.
 */
@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
  // , QueryDslPredicateExecutor<User>
  // CrudRepository

  User findByName(@Param("name") String name);

  List<User> findByRoles_name(String name);
}
