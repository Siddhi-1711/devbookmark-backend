package com.devbookmark.resource;

import com.devbookmark.resource.dto.LinkPreviewResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
public class ResourcePreviewController {

    private final LinkPreviewService linkPreviewService;

    public ResourcePreviewController(LinkPreviewService linkPreviewService) {
        this.linkPreviewService = linkPreviewService;
    }

    @GetMapping("/preview")
    public LinkPreviewResponse preview(@RequestParam String url) {
        return linkPreviewService.preview(url);
    }
}