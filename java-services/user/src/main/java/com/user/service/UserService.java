package com.user.service;

import com.user.dto.CreateUserRequest;
import com.user.dto.PagedResponse;
import com.user.dto.UpdateUserRequest;
import com.user.dto.UserResponse;
import com.user.dto.UserSummaryResponse;
import com.user.entity.UserType;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse create(CreateUserRequest request, String createdBy);

    UserResponse getById(Long id);

    PagedResponse<UserSummaryResponse> search(UserType userType, String keyword, Pageable pageable);

    UserResponse update(Long id, UpdateUserRequest request, String currentKeycloakId);

    UserResponse updateStatus(Long id, boolean enabled, String currentKeycloakId);

    void delete(Long id, String currentKeycloakId);
}
