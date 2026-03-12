package com.devbookmark.search;

import com.devbookmark.common.dto.PageResponse;
import com.devbookmark.resource.ResourceType;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.security.AuthUser;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/resources")
    public PageResponse<ResourceResponse> resources(
            Authentication auth,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "false") boolean publishedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var me = AuthUser.maybeUserId(auth);

        var result = searchService.searchResources(
                me, q, tags, type, author, from, to, sort, publishedOnly, page, size
        );

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    @GetMapping("/suggestions")
    public Map<String, List<String>> suggestions(@RequestParam String q) {
        return searchService.suggestions(q);
    }

    @GetMapping("/authors")
    public List<String> authorSuggestions(@RequestParam String q) {
        return searchService.authorSuggestions(q);
    }
}