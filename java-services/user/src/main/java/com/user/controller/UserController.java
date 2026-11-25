package com.user.controller;

import com.user.dto.CreateUserRequest;
import com.user.dto.PagedResponse;
import com.user.dto.StatusUpdateRequest;
import com.user.dto.UpdateUserRequest;
import com.user.dto.UserResponse;
import com.user.dto.UserSummaryResponse;
import com.user.entity.UserType;
import com.user.security.Permissions;
import com.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize(Permissions.HAS_USER_ADD)
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        UserResponse created = userService.create(request, jwt.getClaimAsString("preferred_username"));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PreAuthorize(Permissions.HAS_USER_VIEW)
    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PreAuthorize(Permissions.HAS_USER_VIEW)
    @GetMapping
    public PagedResponse<UserSummaryResponse> search(
            @RequestParam(required = false) UserType userType,
            @RequestParam(name = "q", required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return userService.search(userType, keyword, pageable);
    }

    @PreAuthorize(Permissions.HAS_USER_EDIT)
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request,
                               @AuthenticationPrincipal Jwt jwt) {
        return userService.update(id, request, jwt.getSubject());
    }

    @PreAuthorize(Permissions.HAS_USER_EDIT)
    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        return userService.updateStatus(id, request.enabled(), jwt.getSubject());
    }

    @PreAuthorize(Permissions.HAS_USER_DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        userService.delete(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
