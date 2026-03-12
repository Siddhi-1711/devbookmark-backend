package com.devbookmark.pin;

import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.security.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pins")
public class PinController {

    private final PinService pinService;

    public PinController(PinService pinService) {
        this.pinService = pinService;
    }

    // Pin resource to slot
    @PostMapping("/{resourceId}/slot/{slot}")
    public void pin(
            Authentication auth,
            @PathVariable UUID resourceId,
            @PathVariable int slot
    ) {
        UUID me = AuthUser.requireUserId(auth);
        pinService.pin(me, resourceId, slot);
    }

    // Unpin slot
    @DeleteMapping("/slot/{slot}")
    public void unpin(
            Authentication auth,
            @PathVariable int slot
    ) {
        UUID me = AuthUser.requireUserId(auth);
        pinService.unpin(me, slot);
    }

    // Get my pinned resources
    @GetMapping("/me")
    public List<ResourceResponse> myPins(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        return pinService.getPinnedResources(me, me); // pass me as viewer
    }

    // Get pinned resources for any user (public)
    @GetMapping("/users/{userId}")
    public List<ResourceResponse> userPins(@PathVariable UUID userId) {
        return pinService.getPinnedResources(userId);
    }
}