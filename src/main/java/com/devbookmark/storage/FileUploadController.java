package com.devbookmark.storage;

import com.devbookmark.security.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileUploadController.class);
    private final FileStorageService storageService;

    public FileUploadController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/file")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        AuthUser.requireUserId(authentication);
        log.info("File received - name: {}, size: {}, type: {}",
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType());
        try {
            String url = storageService.upload(file);

            // ✅ Handle nulls safely
            Map<String, String> response = new LinkedHashMap<>();
            response.put("url", url);
            response.put("name", file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
            response.put("size", String.valueOf(file.getSize()));
            response.put("type", file.getContentType() != null ? file.getContentType() : "unknown");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Invalid request";
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        } catch (IOException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Upload failed";
            return ResponseEntity.internalServerError().body(Map.of("error", msg));
        }
    }
}