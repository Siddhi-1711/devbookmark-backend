package com.devbookmark.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.cloudinary.cloud-name}")
    private String cloudName;

    @Value("${app.cloudinary.api-key}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    @PostConstruct
    public void init() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
        log.info("Cloudinary initialized for cloud: {}", cloudName);
    }

    public String upload(MultipartFile file) throws IOException {

        // 1. Validate file not empty
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        // 2. Validate file size (10 MB max)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10 MB limit.");
        }

        // 3. Validate MIME type — check both declared content-type and magic bytes
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed: " + contentType);
        }

        // 4. Validate magic bytes — don't trust the client-declared content-type alone
        validateMagicBytes(file.getBytes(), contentType);

        // 5. Determine resource type
        // images → "image", everything else → "raw"
        String resourceType = contentType.startsWith("image/") ? "image" : "raw";

        // 6. Upload to Cloudinary
        log.info("Uploading file to Cloudinary - name: {}, type: {}, size: {}",
                file.getOriginalFilename(), contentType, file.getSize());

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", resourceType,
                        "folder", "devbookmark/files",
                        "use_filename", true,
                        "unique_filename", true
                )
        );

        String url = (String) uploadResult.get("secure_url");
        log.info("Upload successful - url: {}", url);
        return transformCloudinaryUrl(url, contentType);
    }
    /**
     * Validates the first bytes of the file against known magic signatures.
     * Prevents renaming a .exe as .pdf to bypass the MIME check.
     */
    private void validateMagicBytes(byte[] bytes, String declaredType) {
        if (bytes == null || bytes.length < 4) return;

        if (declaredType.equals("application/pdf")) {
            if (bytes[0] != 0x25 || bytes[1] != 0x50 || bytes[2] != 0x44 || bytes[3] != 0x46)
                throw new IllegalArgumentException("File content does not match declared type (PDF).");
            return;
        }
        if (declaredType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            if (bytes[0] != 0x50 || bytes[1] != 0x4B)
                throw new IllegalArgumentException("File content does not match declared type (DOCX).");
            return;
        }
        if (declaredType.equals("image/jpeg")) {
            if ((bytes[0] & 0xFF) != 0xFF || (bytes[1] & 0xFF) != 0xD8 || (bytes[2] & 0xFF) != 0xFF)
                throw new IllegalArgumentException("File content does not match declared type (JPEG).");
            return;
        }
        if (declaredType.equals("image/png")) {
            if ((bytes[0] & 0xFF) != 0x89 || bytes[1] != 0x50 || bytes[2] != 0x4E || bytes[3] != 0x47)
                throw new IllegalArgumentException("File content does not match declared type (PNG).");
            return;
        }
        if (declaredType.equals("image/gif")) {
            if (bytes[0] != 'G' || bytes[1] != 'I' || bytes[2] != 'F' || bytes[3] != '8')
                throw new IllegalArgumentException("File content does not match declared type (GIF).");
            return;
        }
        if (declaredType.equals("image/webp")) {
            if (bytes[0] != 'R' || bytes[1] != 'I' || bytes[2] != 'F' || bytes[3] != 'F')
                throw new IllegalArgumentException("File content does not match declared type (WebP).");
            return;
        }
        // DOC / text/plain — no reliable magic bytes, skip
    }

    private String transformCloudinaryUrl(String url, String contentType) {
        // ✅ Do not rewrite URLs for PDFs or raw files
        // Cloudinary already serves PDFs inline when allowed in settings
        return url;
    }
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            // Extract public_id from URL
            // URL format: https://res.cloudinary.com/cloud/raw/upload/v123/devbookmark/files/filename
            String publicId = extractPublicId(fileUrl);
            if (publicId == null) return;

            String resourceType = fileUrl.contains("/image/") ? "image" : "raw";

            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));

            log.info("Deleted file from Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.warn("Failed to delete file from Cloudinary: {}", e.getMessage());
        }
    }

    private String extractPublicId(String url) {
        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1) return null;
            String afterUpload = url.substring(uploadIndex + 8);

            // ✅ Skip transformation flags like fl_inline/
            if (!afterUpload.startsWith("v") && afterUpload.contains("/")) {
                // Could be fl_inline/v123/... — skip flags until we hit vNNNN
                String[] parts = afterUpload.split("/");
                StringBuilder rebuilt = new StringBuilder();
                boolean foundVersion = false;
                for (String part : parts) {
                    if (!foundVersion && part.matches("v\\d+")) {
                        foundVersion = true;
                        continue; // skip version segment
                    }
                    if (foundVersion) {
                        if (rebuilt.length() > 0) rebuilt.append("/");
                        rebuilt.append(part);
                    }
                }
                if (foundVersion) afterUpload = rebuilt.toString();
            } else if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
            }

            // Remove file extension
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex > 0) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }
            return afterUpload.isBlank() ? null : afterUpload;
        } catch (Exception e) {
            log.warn("Could not extract public_id from url: {}", url);
            return null;
        }
    }
    public record FileMeta(String safeFilename, String contentType) {}

    public FileMeta guessMetaFromUrl(String fileUrl, String title) {
        String base = (title == null || title.isBlank()) ? "file" : title.trim();

        // Windows-safe filename
        base = base.replaceAll("[\\\\/:*?\"<>|]", "").trim();
        if (base.isBlank()) base = "file";
        if (base.length() > 80) base = base.substring(0, 80);

        String lower = fileUrl.toLowerCase();

        String ext;
        String ct;

        if (lower.contains(".pdf")) {
            ext = "pdf"; ct = "application/pdf";
        } else if (lower.contains(".docx")) {
            ext = "docx"; ct = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lower.contains(".doc")) {
            ext = "doc"; ct = "application/msword";
        } else if (lower.contains(".txt")) {
            ext = "txt"; ct = "text/plain";
        } else {
            // fallback: still download as file.pdf so Windows recognizes it
            ext = "pdf"; ct = "application/pdf";
        }

        return new FileMeta(base + "." + ext, ct);
    }
    public FileMeta guessMetaFromContentType(String contentType, String title) {
        String base = (title == null || title.isBlank()) ? "file" : title.trim();
        base = base.replaceAll("[\\\\/:*?\"<>|]", "").trim();
        if (base.isBlank()) base = "file";
        if (base.length() > 80) base = base.substring(0, 80);

        String ct = contentType.toLowerCase();
        String ext;

        if (ct.contains("pdf")) {
            ext = "pdf";
            contentType = "application/pdf";
        } else if (ct.contains("wordprocessingml")) {
            ext = "docx";
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (ct.contains("msword")) {
            ext = "doc";
            contentType = "application/msword";
        } else if (ct.contains("text/plain")) {
            ext = "txt";
            contentType = "text/plain; charset=utf-8";
        } else {
            ext = "bin";
            contentType = "application/octet-stream";
        }

        return new FileMeta(base + "." + ext, contentType);
    }
    public FileMeta guessMetaFromStored(String storedContentType, String storedFileName, String title, String fileUrl) {
        // Prefer original filename if we have it
        String base = (storedFileName != null && !storedFileName.isBlank())
                ? storedFileName.trim()
                : ((title == null || title.isBlank()) ? "file" : title.trim());

        base = base.replaceAll("[\\\\/:*?\"<>|]", "").trim();
        if (base.isBlank()) base = "file";
        if (base.length() > 120) base = base.substring(0, 120);

        // If filename already has extension, keep it
        String lowerName = base.toLowerCase();
        boolean hasExt = lowerName.matches(".*\\.(pdf|docx|doc|txt)$");

        String ct = (storedContentType == null) ? "" : storedContentType.toLowerCase();
        String ext;

        if (hasExt) {
            // keep ext, just set a best-effort content-type
            if (lowerName.endsWith(".pdf")) ext = "pdf";
            else if (lowerName.endsWith(".docx")) ext = "docx";
            else if (lowerName.endsWith(".doc")) ext = "doc";
            else ext = "txt";
        } else {
            // Decide ext by stored type, fallback to URL
            String urlLower = (fileUrl == null) ? "" : fileUrl.toLowerCase();

            if (ct.contains("pdf") || urlLower.contains(".pdf")) ext = "pdf";
            else if (ct.contains("wordprocessingml") || urlLower.contains(".docx")) ext = "docx";
            else if (ct.contains("msword") || urlLower.contains(".doc")) ext = "doc";
            else if (ct.contains("text/plain") || urlLower.contains(".txt")) ext = "txt";
            else ext = "bin";

            base = base + "." + ext;
        }

        // Final content-type
        String contentType;
        if ("pdf".equals(ext)) contentType = "application/pdf";
        else if ("docx".equals(ext)) contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        else if ("doc".equals(ext)) contentType = "application/msword";
        else if ("txt".equals(ext)) contentType = "text/plain; charset=utf-8";
        else contentType = "application/octet-stream";

        return new FileMeta(base, contentType);
    }
}