package com.kkmall.account.application;

import com.kkmall.account.domain.PhoneNumber;
import com.kkmall.account.domain.Role;
import com.kkmall.account.domain.User;
import com.kkmall.account.domain.UserRepository;
import com.kkmall.account.interfaces.dto.LoginResultDto;
import com.kkmall.account.interfaces.dto.MockCodeResultDto;
import com.kkmall.common.exception.BusinessException;
import com.kkmall.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务，处理登录和模拟验证码。
 */
@Service
public class AuthApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AuthApplicationService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AccountProfileApplicationService profileService;
    private final String mockCode;

    public AuthApplicationService(UserRepository userRepository,
                                  JwtService jwtService,
                                  AccountProfileApplicationService profileService,
                                  @Value("${kkmall.mock.code}") String mockCode) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.profileService = profileService;
        this.mockCode = mockCode;
    }

    public MockCodeResultDto mockCode(String phone) {
        PhoneNumber.of(phone);
        MockCodeResultDto result = new MockCodeResultDto();
        result.setMockCodeSent(true);
        return result;
    }

    @Transactional
    public LoginResultDto login(String phone, String code) {
        PhoneNumber phoneNumber = PhoneNumber.of(phone);
        if (!mockCode.equals(code)) {
            throw new BusinessException("AUTH_INVALID_CODE");
        }
        User user = userRepository.findByPhone(phoneNumber.value()).orElse(null);
        if (user == null) {
            user = User.create(phoneNumber, "用户" + phoneNumber.value().substring(7), Role.CUSTOMER);
            userRepository.save(user);
            log.info("新用户注册，userId={}, phone={}", user.getId(), phoneNumber.value());
        }
        return buildLoginResult(user);
    }

    public LoginResultDto adminLogin(String phone, String code) {
        PhoneNumber phoneNumber = PhoneNumber.of(phone);
        if (!mockCode.equals(code)) {
            throw new BusinessException("AUTH_INVALID_CODE");
        }
        User user = userRepository.findByPhone(phoneNumber.value()).orElse(null);
        if (user == null || user.getRole() != Role.ADMIN) {
            log.warn("管理员登录失败，phone={}", phoneNumber.value());
            throw new BusinessException("AUTH_FORBIDDEN", HttpStatus.FORBIDDEN);
        }
        return buildLoginResult(user);
    }

    private LoginResultDto buildLoginResult(User user) {
        LoginResultDto result = new LoginResultDto();
        result.setToken(jwtService.issue(user.getId(), user.getPhone().value(), user.getRole().name()));
        result.setUser(profileService.toDto(user));
        return result;
    }
}
