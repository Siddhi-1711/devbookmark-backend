package com.devbookmark.resource;

import com.devbookmark.resource.dto.*;
import com.devbookmark.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/resources")
public class ResourceCommentController {

    private final ResourceCommentService commentService;

    public ResourceCommentController(ResourceCommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{id}/comments")
    public CommentResponse add(Authentication auth, @PathVariable UUID id,
                               @Valid @RequestBody CommentCreateRequest req) {
        UUID me = AuthUser.maybeUserId(auth);
        return commentService.add(me, id, req);
    }

    @GetMapping("/{id}/comments")
    public Page<CommentResponse> list(Authentication auth, @PathVariable UUID id,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        UUID me = AuthUser.maybeUserId(auth);  // ← change this line
        return commentService.list(me, id, page, size);
    }

    @PutMapping("/comments/{commentId}")
    public CommentResponse update(Authentication auth, @PathVariable UUID commentId,
                                  @Valid @RequestBody CommentUpdateRequest req) {
        UUID me = AuthUser.requireUserId(auth);
        return commentService.update(me, commentId, req);
    }

    @DeleteMapping("/comments/{commentId}")
    public void delete(Authentication auth, @PathVariable UUID commentId) {
        UUID me = AuthUser.requireUserId(auth);
        commentService.delete(me, commentId);
    }
}