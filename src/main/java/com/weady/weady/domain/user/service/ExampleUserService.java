package com.weady.weady.domain.user.service;

import com.weady.weady.domain.user.dto.response.ExampleUserResponse;
import com.weady.weady.domain.user.entity.User;
import com.weady.weady.domain.user.mapper.UserMapper;
import com.weady.weady.domain.user.repository.UserRepository;
import com.weady.weady.common.error.errorCode.UserErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import com.weady.weady.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExampleUserService {
    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자의 정보를 반환
     * @return ExampleUserResponseDto
     * @thorws UserErrorCode.USER_NOT_FOUND 사용자가 존재하지 않을 경우 예외를 발생
     */
    public ExampleUserResponse.ExampleUserResponseDto getUser(){

        Long userId = SecurityUtil.getCurrentUserId(); //SecurityContext 에서 현재 로그인한 사용자의 ID를 가져옴

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        return UserMapper.toResponseDto(user);
    }
}
