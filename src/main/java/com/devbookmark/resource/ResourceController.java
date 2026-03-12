package com.devbookmark.resource;

import com.devbookmark.common.dto.PageResponse;
import com.devbookmark.resource.dto.ResourceCreateRequest;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.resource.dto.ResourceUpdateRequest;
import com.devbookmark.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResourceResponse create(Authentication authentication, @Valid @RequestBody ResourceCreateRequest req) {
        UUID userId = AuthUser.requireUserId(authentication);
        return resourceService.create(userId, req);
    }

    // ✅ MOVE THIS BEFORE /{id} ENDPOINT
    @GetMapping("/my-saved")
    @Transactional(readOnly = true)
    public ResponseEntity<PageResponse<ResourceResponse>> getMySaved(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Authentication authentication
    ) {
        UUID userId = AuthUser.requireUserId(authentication);
        PageResponse<ResourceResponse> response = resourceService.getMySaved(userId, page, size);
        return ResponseEntity.ok(response);
    }

    // ✅ NOW PUT /{id} AFTER
    @GetMapping("/{id:[0-9a-fA-F\\-]{36}}")
    public ResourceResponse get(@PathVariable UUID id) {
        return resourceService.getById(id);
    }

    @PutMapping("/{id}")
    public ResourceResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody ResourceUpdateRequest req,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return resourceService.update(userId, id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id, Authentication authentication) {
        UUID userId = AuthUser.requireUserId(authentication);
        resourceService.delete(userId, id);
    }
}