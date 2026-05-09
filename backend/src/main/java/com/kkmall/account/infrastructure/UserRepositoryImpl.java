package com.kkmall.account.infrastructure;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkmall.account.domain.User;
import com.kkmall.account.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现，负责 PO ↔ Domain Model 转换。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserConverter userConverter;

    public UserRepositoryImpl(UserMapper userMapper, UserConverter userConverter) {
        this.userMapper = userMapper;
        this.userConverter = userConverter;
    }

    @Override
    public Optional<User> findById(Long id) {
        UserPo po = userMapper.selectById(id);
        return Optional.ofNullable(userConverter.toDomain(po));
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        UserPo po = userMapper.selectOne(
                new QueryWrapper<UserPo>().eq("phone", phone).last("LIMIT 1"));
        return Optional.ofNullable(userConverter.toDomain(po));
    }

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            UserPo po = userConverter.toPo(user);
            userMapper.insert(po);
            user.assignId(po.getId());
        } else {
            UserPo po = userMapper.selectById(user.getId());
            if (po != null) {
                userConverter.updatePo(user, po);
                userMapper.updateById(po);
            }
        }
    }
}
