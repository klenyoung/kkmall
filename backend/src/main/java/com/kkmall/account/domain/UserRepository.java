package com.kkmall.account.domain;

import java.util.Optional;

/**
 * 用户仓储接口，定义在领域层，由基础设施层实现。
 */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByPhone(String phone);

    void save(User user);
}
