package com.devbookmark.resource;

import com.devbookmark.resource.dto.BatchResourceRequest;
import com.devbookmark.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/resources/batch")
public class ResourceBatchController {

    private final ResourceBatchService batchService;

    public ResourceBatchController(ResourceBatchService batchService) {
        this.batchService = batchService;
    }

    /**
     * Like multiple resources at once
     */
    @PostMapping("/like")
    public void batchLike(
            Authentication auth,
            @Valid @RequestBody BatchResourceRequest req
    ) {
        UUID me = AuthUser.requireUserId(auth);
        batchService.batchLike(me, req.getResourceIds());
    }

    /**
     * Save multiple resources at once
     */
    @PostMapping("/save")
    public void batchSave(
            Authentication auth,
            @Valid @RequestBody BatchResourceRequest req
    ) {
        UUID me = AuthUser.requireUserId(auth);
        batchService.batchSave(me, req.getResourceIds());
    }

    /**
     * Unlike multiple resources
     */
    @PostMapping("/unlike")
    public void batchUnlike(
            Authentication auth,
            @Valid @RequestBody BatchResourceRequest req
    ) {
        UUID me = AuthUser.requireUserId(auth);
        batchService.batchUnlike(me, req.getResourceIds());
    }

    /**
     * Unsave multiple resources
     */
    @PostMapping("/unsave")
    public void batchUnsave(
            Authentication auth,
            @Valid @RequestBody BatchResourceRequest req
    ) {
        UUID me = AuthUser.requireUserId(auth);
        batchService.batchUnsave(me, req.getResourceIds());
    }

    /**
     * Delete multiple resources
     */
    @PostMapping("/delete")
    public void batchDelete(
            Authentication auth,
            @Valid @RequestBody BatchResourceRequest req
    ) {
        UUID me = AuthUser.requireUserId(auth);
        batchService.batchDelete(me, req.getResourceIds());
    }
}