package com.devbookmark.resource;

import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceInteractionController {

    private final ResourceInteractionService interactionService;
    private final ResourceService resourceService;

    public ResourceInteractionController(ResourceInteractionService interactionService,
                                         ResourceService resourceService) {
        this.interactionService = interactionService;
        this.resourceService = resourceService;
    }

    @PostMapping("/{id}/like")
    public void like(Authentication auth, @PathVariable UUID id) {
        interactionService.like(AuthUser.requireUserId(auth), id);
    }

    @DeleteMapping("/{id}/like")
    public void unlike(Authentication auth, @PathVariable UUID id) {
        interactionService.unlike(AuthUser.requireUserId(auth), id);
    }

    @PostMapping("/{id}/save")
    public void save(Authentication auth, @PathVariable UUID id) {
        interactionService.save(AuthUser.requireUserId(auth), id);
    }

    @DeleteMapping("/{id}/save")
    public void unsave(Authentication auth, @PathVariable UUID id) {
        interactionService.unsave(AuthUser.requireUserId(auth), id);
    }



}