package com.devbookmark.storage;

import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceVisibility;
import com.devbookmark.security.AuthUser;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileDownloadController {

    private static final long MAX_PROXY_BYTES = 20 * 1024 * 1024;

    private final ResourceRepository resourceRepository;
    private final FileStorageService fileStorageService;

    public FileDownloadController(ResourceRepository resourceRepository,
                                  FileStorageService fileStorageService) {
        this.resourceRepository = resourceRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/resource/{resourceId}/download")
    @Transactional(readOnly = true)
    public void download(
            @PathVariable UUID resourceId,
            org.springframework.security.core.Authentication auth,
            HttpServletResponse response
    ) throws Exception {

        Resource r = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        String fileUrl = r.getFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No file attached");
            return;
        }

        if (!canAccess(r, auth, response)) return;

        fileUrl = fileUrl.replace("/raw/upload/fl_inline/", "/raw/upload/");

        java.net.URL url = new java.net.URL(fileUrl);
        java.net.URLConnection conn = url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        FileStorageService.FileMeta meta = fileStorageService.guessMetaFromStored(
                r.getFileContentType(), r.getFileName(), r.getTitle(), fileUrl);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + meta.safeFilename() + "\"");
        response.setContentType(meta.contentType());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private, max-age=60");

        long contentLength = conn.getContentLengthLong();
        if (contentLength > MAX_PROXY_BYTES) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "File too large");
            return;
        }

        try (InputStream in = conn.getInputStream()) {
            in.transferTo(response.getOutputStream());
            response.flushBuffer();
        }
    }

    @GetMapping("/resource/{resourceId}/view")
    @Transactional(readOnly = true)
    public void view(
            @PathVariable UUID resourceId,
            org.springframework.security.core.Authentication auth,
            HttpServletResponse response
    ) throws Exception {

        Resource r = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        String fileUrl = r.getFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No file attached");
            return;
        }

        if (!canAccess(r, auth, response)) return;

        fileUrl = fileUrl.replace("/raw/upload/fl_inline/", "/raw/upload/");

        var url = new java.net.URL(fileUrl);
        var conn = url.openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        FileStorageService.FileMeta meta = fileStorageService.guessMetaFromStored(
                r.getFileContentType(), r.getFileName(), r.getTitle(), fileUrl);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + meta.safeFilename() + "\"");
        response.setContentType(meta.contentType());
        response.setHeader("X-Content-Type-Options", "nosniff");

        long contentLength = conn.getContentLengthLong();
        if (contentLength > MAX_PROXY_BYTES) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "File too large");
            return;
        }

        try (java.io.InputStream in = conn.getInputStream()) {
            in.transferTo(response.getOutputStream());
            response.flushBuffer();
        }
    }

    private boolean canAccess(Resource r,
                              org.springframework.security.core.Authentication auth,
                              HttpServletResponse response) throws IOException {

        ResourceVisibility vis = r.getVisibility();

        if (vis == null || vis == ResourceVisibility.PUBLIC) return true;

        UUID viewerId = AuthUser.maybeUserId(auth);

        if (viewerId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return false;
        }

        // Safe because calling methods are @Transactional
        UUID ownerId = r.getOwner().getId();
        if (viewerId.equals(ownerId)) return true;

        if (vis == ResourceVisibility.PRIVATE) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
            return false;
        }

        return true; // FOLLOWERS — any logged-in user
    }
}