package com.kkmall.account.infrastructure;

import com.kkmall.account.domain.Gender;
import com.kkmall.account.domain.PhoneNumber;
import com.kkmall.account.domain.Role;
import com.kkmall.account.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * UserPo 与 User 领域对象之间的转换器。
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * PO → Domain Model
     */
    default User toDomain(UserPo po) {
        if (po == null) {
            return null;
        }
        return User.restore(
                po.getId(),
                PhoneNumber.of(po.getPhone()),
                po.getNickname(),
                po.getAvatarUrl(),
                Gender.of(po.getGender()),
                po.getBirthday(),
                Role.valueOf(po.getRole()),
                po.getCreatedAt()
        );
    }

    /**
     * Domain Model → PO（用于新增）
     */
    default UserPo toPo(User user) {
        if (user == null) {
            return null;
        }
        UserPo po = new UserPo();
        po.setId(user.getId());
        po.setPhone(user.getPhone().value());
        po.setNickname(user.getNickname());
        po.setAvatarUrl(user.getAvatarUrl());
        po.setGender(user.getGender() == null ? "UNKNOWN" : user.getGender().name());
        po.setBirthday(user.getBirthday());
        po.setRole(user.getRole().name());
        return po;
    }

    /**
     * 将 Domain Model 的变更同步到已有 PO（用于更新）。
     */
    default void updatePo(User user, UserPo po) {
        po.setNickname(user.getNickname());
        po.setAvatarUrl(user.getAvatarUrl());
        po.setGender(user.getGender() == null ? "UNKNOWN" : user.getGender().name());
        po.setBirthday(user.getBirthday());
    }
}
