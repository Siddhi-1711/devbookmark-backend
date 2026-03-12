package com.devbookmark.resource.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class LinkPreviewResponse {
    private String url;
    private String title;
    private String description;
    private String image;
    private String siteName;
    private String type;
}