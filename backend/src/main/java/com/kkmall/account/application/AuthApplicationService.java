package com.kkmall.account.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kkmall.account.domain.PhoneNumber;
import com.kkmall.account.domain.Role;
import com.kkmall.account.infrastructure.UserMapper;
import com.kkmall.account.infrastructure.UserPo;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthApplicationService {
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final String mockCode;

    public AuthApplicationService(UserMapper userMapper, JwtService jwtService, @Value("${kkmall.mock.code}") String mockCode) {
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.mockCode = mockCode;
    }

    public Map<String, Object> mockCode(String phone) {
        PhoneNumber.of(phone);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mockCodeSent", true);
        return data;
    }

    @Transactional
    public Map<String, Object> login(String phone, String code) {
        PhoneNumber phoneNumber = PhoneNumber.of(phone);
        if (!mockCode.equals(code)) throw new BusinessException("AUTH_INVALID_CODE");
        UserPo user = findByPhone(phoneNumber.value());
        if (user == null) {
            user = new UserPo();
            user.phone = phoneNumber.value();
            user.nickname = "用户" + phoneNumber.value().substring(7);
            user.role = Role.CUSTOMER.name();
            userMapper.insert(user);
        }
        return authPayload(user);
    }

    public Map<String, Object> adminLogin(String phone, String code) {
        PhoneNumber phoneNumber = PhoneNumber.of(phone);
        if (!mockCode.equals(code)) throw new BusinessException("AUTH_INVALID_CODE");
        UserPo user = findByPhone(phoneNumber.value());
        if (user == null || !Role.ADMIN.name().equals(user.role)) {
            throw new BusinessException("AUTH_FORBIDDEN", HttpStatus.FORBIDDEN);
        }
        return authPayload(user);
    }

    private UserPo findByPhone(String phone) {
        return userMapper.selectOne(new QueryWrapper<UserPo>().eq("phone", phone).last("LIMIT 1"));
    }

    private Map<String, Object> authPayload(UserPo user) {
        Map<String, Object> userView = new LinkedHashMap<>();
        userView.put("id", user.id);
        userView.put("phone", user.phone);
        userView.put("nickname", user.nickname);
        userView.put("role", user.role);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", jwtService.issue(user.id, user.phone, user.role));
        data.put("user", userView);
        return data;
    }
}
