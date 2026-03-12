package com.devbookmark.publication;

import com.devbookmark.publication.dto.PublicationRequest;
import com.devbookmark.publication.dto.PublicationResponse;
import com.devbookmark.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/publications")
public class PublicationController {

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    // Create my publication
    @PostMapping
    public PublicationResponse create(
            Authentication auth,
            @Valid @RequestBody PublicationRequest req
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return publicationService.create(me, req);
    }

    // Update my publication
    @PutMapping
    public PublicationResponse update(
            Authentication auth,
            @Valid @RequestBody PublicationRequest req
    ) {
        UUID me = AuthUser.requireUserId(auth);
        return publicationService.update(me, req);
    }

    // Get my publication
    @GetMapping("/me")
    public PublicationResponse myPublication(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        return publicationService.getMyPublication(me);
    }

    // Get publication by slug (public)
    @GetMapping("/{slug}")
    public PublicationResponse getBySlug(@PathVariable String slug) {
        return publicationService.getBySlug(slug);
    }

    // Delete my publication
    @DeleteMapping
    public void delete(Authentication auth) {
        UUID me = AuthUser.requireUserId(auth);
        publicationService.delete(me);
    }
}