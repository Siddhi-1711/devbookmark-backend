package com.devbookmark.tag;

import com.devbookmark.tag.dto.TrendingTagResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // Trending tags in last N days
    @GetMapping("/trending")
    public List<TrendingTagResponse> trending(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return tagService.getTrendingTags(days, limit);
    }

    // All time popular tags
    @GetMapping("/popular")
    public List<TrendingTagResponse> popular(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return tagService.getPopularTags(limit);
    }
}