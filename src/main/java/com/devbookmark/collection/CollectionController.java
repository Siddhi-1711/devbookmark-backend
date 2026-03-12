package com.devbookmark.collection;

import com.devbookmark.collection.dto.CollectionCreateRequest;
import com.devbookmark.collection.dto.CollectionDetailResponse;
import com.devbookmark.collection.dto.CollectionSummaryResponse;
import com.devbookmark.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.devbookmark.collection.dto.CollectionUpdateRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    public CollectionSummaryResponse create(Authentication auth, @Valid @RequestBody CollectionCreateRequest req) {
        UUID userId = AuthUser.requireUserId(auth);
        return collectionService.create(userId, req);
    }

    @GetMapping("/me")
    public List<CollectionSummaryResponse> myCollections(Authentication auth) {
        UUID userId = AuthUser.requireUserId(auth);
        return collectionService.myCollections(userId);
    }

    @PostMapping("/{collectionId}/resources/{resourceId}")
    public void add(Authentication auth, @PathVariable UUID collectionId, @PathVariable UUID resourceId) {
        UUID userId = AuthUser.requireUserId(auth);
        collectionService.addResource(userId, collectionId, resourceId);
    }

    @DeleteMapping("/{collectionId}/resources/{resourceId}")
    public void remove(Authentication auth, @PathVariable UUID collectionId, @PathVariable UUID resourceId) {
        UUID userId = AuthUser.requireUserId(auth);
        collectionService.removeResource(userId, collectionId, resourceId);
    }

    // viewer can be null if unauthenticated => only public collections viewable
    @GetMapping("/{collectionId}")
    public CollectionDetailResponse detail(Authentication auth, @PathVariable UUID collectionId) {
        UUID viewerId = AuthUser.maybeUserId(auth);
        return collectionService.getDetail(viewerId, collectionId);
    }

    @PutMapping("/{collectionId}")
    public CollectionSummaryResponse update(Authentication auth,
                                            @PathVariable UUID collectionId,
                                            @Valid @RequestBody CollectionUpdateRequest req) {
        UUID userId = AuthUser.requireUserId(auth);
        return collectionService.update(userId, collectionId, req);
    }

    @DeleteMapping("/{collectionId}")
    public void delete(Authentication auth, @PathVariable UUID collectionId) {
        UUID userId = AuthUser.requireUserId(auth);
        collectionService.delete(userId, collectionId);
    }
}