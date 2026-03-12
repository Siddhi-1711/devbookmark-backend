package com.devbookmark.resource;

import com.devbookmark.activity.ActivityRepository;
import com.devbookmark.activity.ActivityService;
import com.devbookmark.common.dto.PageResponse;
import com.devbookmark.readinglist.ReadingListRepository;
import com.devbookmark.repost.RepostRepository;
import com.devbookmark.resource.dto.ResourceCreateRequest;
import com.devbookmark.resource.dto.ResourceResponse;
import com.devbookmark.resource.dto.ResourceUpdateRequest;
import com.devbookmark.series.SeriesItemRepository;
import com.devbookmark.storage.FileStorageService;
import com.devbookmark.tag.Tag;
import com.devbookmark.tag.TagRepository;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ResourceService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResourceService.class);
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final LinkPreviewService linkPreviewService;
    private final RepostRepository repostRepository;
    private final ResourceViewRepository viewRepository;
    private final ResourceLikeRepository likeRepository;
    private final ResourceSaveRepository saveRepository;
    private final ActivityService activityService;
    private final ActivityRepository activityRepository;
    private final SeriesItemRepository seriesItemRepository;
    private final FileStorageService fileStorageService;
    private final ReadingListRepository readingListRepository;

    public ResourceService(

            ResourceRepository resourceRepository,
            UserRepository userRepository,
            TagRepository tagRepository,
            LinkPreviewService linkPreviewService,
            ResourceLikeRepository likeRepository,
            ResourceSaveRepository saveRepository,
            @Lazy ActivityService activityService,
            ActivityRepository activityRepository,
            SeriesItemRepository seriesItemRepository,
            RepostRepository repostRepository,
            ResourceViewRepository viewRepository,
            FileStorageService fileStorageService,
            ReadingListRepository readingListRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.linkPreviewService = linkPreviewService;
        this.likeRepository = likeRepository;
        this.saveRepository = saveRepository;
        this.activityService = activityService;
        this.activityRepository = activityRepository;
        this.seriesItemRepository = seriesItemRepository;
        this.repostRepository = repostRepository;
        this.viewRepository = viewRepository;
        this.fileStorageService = fileStorageService;
        this.readingListRepository = readingListRepository;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @Transactional
    public ResourceResponse create(UUID ownerId, ResourceCreateRequest req) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String link = (req.link() == null) ? null : req.link().trim();

        // link only required for non-written posts
        if (isBlank(link) && req.type() != ResourceType.WRITTEN_POST && isBlank(req.fileUrl())) {
            throw new IllegalArgumentException("Link or file is required for this resource type.");
        }

        String title = (req.title() == null) ? null : req.title().trim();
        String desc  = (req.description() == null) ? null : req.description().trim();

        if (isBlank(title) || isBlank(desc)) {
            var preview = linkPreviewService.preview(link);
            if (isBlank(title)) title = preview.getTitle();
            if (isBlank(desc))  desc  = preview.getDescription();
        }
        if (isBlank(title)) title = "Untitled";

        Resource resource = Resource.builder()
                .owner(owner)
                .title(title)
                .description(desc)
                .link(link)
                .fileUrl(req.fileUrl())
                .fileName(req.fileName())
                .fileContentType(req.fileContentType())
                .type(req.type())
                .visibility(
                        req.visibility() == null
                                ? ResourceVisibility.PUBLIC
                                : req.visibility()
                )
                .content(req.content())
                .coverImage(req.coverImage())

                .readTimeMinutes(calculateReadTime(req.content()))
                .build();

        Set<String> normalized = normalizeTags(req.tags());
        for (String t : normalized) {
            Tag tag = tagRepository.findByName(t)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(t).build()));

            resource.getResourceTags().add(
                    ResourceTag.builder()
                            .resource(resource)
                            .tag(tag)
                            .build()
            );
        }

        Resource saved = resourceRepository.save(resource);
        activityService.logActivityResourceCreated(ownerId, saved);
        return toResponseBasic(saved, 0L, 0L, false, false, 0L, 0L);
    }

    @Transactional(readOnly = true)
    public ResourceResponse getById(UUID id) {
        Resource r = resourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        long likeCount = likeRepository.countByResourceId(id);
        long saveCount = saveRepository.countByResourceId(id);

        return toResponseBasic(r, likeCount, saveCount, false, false,
                repostRepository.countByResourceId(id),
                viewRepository.countByResourceId(id));
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> latest(UUID me, Pageable pageable) {

        Page<Resource> page;

        if (me == null) {
            page = resourceRepository.findByVisibilityOrderByCreatedAtDesc(
                    ResourceVisibility.PUBLIC, pageable
            );
        } else {
            page = resourceRepository.findByVisibilityInOrderByCreatedAtDesc(
                    Set.of(ResourceVisibility.PUBLIC, ResourceVisibility.FOLLOWERS),
                    pageable
            );
        }

        return enrichPage(page, me);
    }

    @Transactional
    public ResourceResponse update(UUID ownerId, UUID resourceId, ResourceUpdateRequest req) {
        Resource r = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        if (!r.getOwner().getId().equals(ownerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed");
        }

        if (!isBlank(req.title())) r.setTitle(req.title().trim());
        if (req.description() != null) r.setDescription(req.description().trim());
        if (!isBlank(req.link())) r.setLink(req.link().trim());
        if (req.type() != null) r.setType(req.type());

        if (req.tags() != null) {
            r.getResourceTags().clear();
            resourceRepository.saveAndFlush(r);
            Set<String> normalized = normalizeTags(req.tags());
            for (String t : normalized) {
                Tag tag = tagRepository.findByName(t)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(t).build()));

                r.getResourceTags().add(
                        ResourceTag.builder()
                                .resource(r)
                                .tag(tag)
                                .build()
                );
            }
        }
        if (req.visibility() != null) r.setVisibility(req.visibility());
        if (req.content() != null) {
            r.setContent(req.content());
            r.setReadTimeMinutes(calculateReadTime(req.content()));
        }
        if (req.coverImage() != null) r.setCoverImage(req.coverImage());
        if (req.fileUrl() != null) r.setFileUrl(req.fileUrl());
        if (req.fileName() != null) r.setFileName(req.fileName());
        if (req.fileContentType() != null) r.setFileContentType(req.fileContentType());
        if (req.publish() != null) {
            r.setPublished(req.publish());
            if (req.publish() && r.getPublishedAt() == null) {
                r.setPublishedAt(Instant.now());
            }
        }

        long likeCount = likeRepository.countByResourceId(resourceId);
        long saveCount = saveRepository.countByResourceId(resourceId);
        boolean likedByMe = likeRepository.findByUserIdAndResourceId(ownerId, resourceId).isPresent();
        boolean savedByMe = saveRepository.findByUserIdAndResourceId(ownerId, resourceId).isPresent();

        return toResponseBasic(r, likeCount, saveCount, likedByMe, savedByMe,
                repostRepository.countByResourceId(resourceId),
                viewRepository.countByResourceId(resourceId));
    }

    @Transactional
    public void delete(UUID ownerId, UUID resourceId) {
        Resource r = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        if (!r.getOwner().getId().equals(ownerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Not allowed");
        }

        if (r.getFileUrl() != null) {
            try { fileStorageService.delete(r.getFileUrl()); } catch (Exception e) {
                log.warn("Cloudinary delete failed: {}", e.getMessage());
            }
        }

        activityRepository.nullifyResource(resourceId);
        viewRepository.deleteByResourceId(resourceId);   // ✅ fix FK violation
        repostRepository.deleteByResourceId(resourceId); // ✅ prevent same issue for reposts
        readingListRepository.deleteByResourceId(resourceId);
        resourceRepository.delete(r);
    }
    // ---------------- ENRICH HELPERS ----------------

    public Page<ResourceResponse> enrichPage(Page<Resource> page, UUID me) {
        List<Resource> content = page.getContent();
        List<ResourceResponse> enriched = enrichList(content, me);

        Map<UUID, ResourceResponse> responseMap = new HashMap<>();
        for (int i = 0; i < content.size(); i++) {
            responseMap.put(content.get(i).getId(), enriched.get(i));
        }

        return page.map(r -> responseMap.get(r.getId()));
    }

    public List<ResourceResponse> enrichList(List<Resource> resources, UUID me) {
        if (resources == null || resources.isEmpty()) return List.of();

        List<UUID> ids = resources.stream().map(Resource::getId).toList();

        Map<UUID, com.devbookmark.series.projection.ResourceSeriesInfo> seriesMap = new HashMap<>();
        for (var row : seriesItemRepository.findSeriesInfoByResourceIds(ids)) {
            seriesMap.put(row.getResourceId(), row);
        }
        Map<UUID, Long> likeMap = new HashMap<>();
        for (var row : likeRepository.countByResourceIds(ids)) {
            likeMap.put(row.getId(), row.getCnt());
        }

        Map<UUID, Long> saveMap = new HashMap<>();
        for (var row : saveRepository.countByResourceIds(ids)) {
            saveMap.put(row.getId(), row.getCnt());
        }

        Set<UUID> likedSet = Set.of();
        Set<UUID> savedSet = Set.of();
        Set<UUID> repostedSet = Set.of();
        if (me != null) {
            likedSet = new HashSet<>(likeRepository.findLikedResourceIdsByUser(me, ids));
            savedSet = new HashSet<>(saveRepository.findSavedResourceIdsByUser(me, ids));
            repostedSet = new HashSet<>(repostRepository.findRepostedResourceIdsByUser(me, ids));
        }

        Map<UUID, Long> repostMap = new HashMap<>();
        for (var row : repostRepository.countByResourceIds(ids)) {
            repostMap.put(row.getId(), row.getCnt());
        }

        Map<UUID, Long> viewMap = new HashMap<>();
        for (var row : viewRepository.countByResourceIds(ids)) {
            viewMap.put(row.getId(), row.getCnt());
        }

        List<ResourceResponse> out = new ArrayList<>(resources.size());
        for (Resource r : resources) {
            UUID id = r.getId();
            long likeCount = likeMap.getOrDefault(id, 0L);
            long saveCount = saveMap.getOrDefault(id, 0L);
            boolean likedByMe = me != null && likedSet.contains(id);
            boolean savedByMe = me != null && savedSet.contains(id);

            var seriesInfo = seriesMap.get(id);

            out.add(new ResourceResponse(
                    r.getId(),
                    r.getOwner().getId(),
                    r.getOwner().getName(),
                    r.getTitle(),
                    r.getDescription(),
                    r.getLink(),
                    r.getType(),
                    new LinkedHashSet<>(r.getResourceTags().stream()
                            .map(rt -> rt.getTag().getName())
                            .toList()),
                    r.getCreatedAt(),
                    null,           // savedAt

                    likeCount,
                    saveCount,
                    likedByMe,
                    savedByMe,

                    r.getContent(),
                    r.getCoverImage(),
                    r.getFileUrl(),
                    r.getFileName(),
                    r.getFileContentType(),
                    r.getReadTimeMinutes(),
                    r.isPublished(),
                    r.getPublishedAt(),
                    r.getVisibility(),

                    seriesInfo != null ? seriesInfo.getSlug() : null,
                    seriesInfo != null ? seriesInfo.getTitle() : null,
                    seriesInfo != null ? seriesInfo.getPartNumber() : null,
                    repostMap.getOrDefault(id, 0L),
                    me != null && repostedSet.contains(id),
                    viewMap.getOrDefault(id, 0L)
            ));
        }
        return out;
    }

    private ResourceResponse toResponseBasic(Resource r,
                                             long likeCount,
                                             long saveCount,
                                             boolean likedByMe,
                                             boolean savedByMe,
                                             long repostCount,
                                             long viewCount) {
        Set<String> tags = new LinkedHashSet<>();
        for (ResourceTag rt : r.getResourceTags()) {
            tags.add(rt.getTag().getName());
        }

        return new ResourceResponse(
                r.getId(),
                r.getOwner().getId(),
                r.getOwner().getName(),
                r.getTitle(),
                r.getDescription(),
                r.getLink(),
                r.getType(),
                tags,
                r.getCreatedAt(),
                null,
                likeCount,
                saveCount,
                likedByMe,
                savedByMe,
                r.getContent(),
                r.getCoverImage(),
                r.getFileUrl(),
                r.getFileName(),
                r.getFileContentType(),
                r.getReadTimeMinutes(),
                r.isPublished(),
                r.getPublishedAt(),
                r.getVisibility(),
                null,        // seriesSlug
                null,        // seriesTitle
                null,        // seriesPartNumber
                repostCount,
                false,       // repostedByMe
                viewCount
        );
    }

    public ResourceResponse toResponse(Resource r) {
        long likeCount = likeRepository.countByResourceId(r.getId());
        long saveCount = saveRepository.countByResourceId(r.getId());
        long repostCount = repostRepository.countByResourceId(r.getId());
        long viewCount = viewRepository.countByResourceId(r.getId());
        return toResponseBasic(r, likeCount, saveCount, false, false, repostCount, viewCount);
    }

    private Set<String> normalizeTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) return Set.of();
        Set<String> out = new LinkedHashSet<>();
        for (String t : tags) {
            if (t == null) continue;
            String s = t.trim().toLowerCase();
            if (s.isBlank()) continue;
            s = s.replaceAll("\\s+", "-");
            out.add(s);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> profileResources(UUID me, UUID userId, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);

        var pageable = PageRequest.of(safePage, safeSize);
        var resources = resourceRepository.findProfileResources(userId, pageable);

        return enrichList(resources, me);
    }

    @Transactional(readOnly = true)
    public PageResponse<ResourceResponse> getMySaved(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        var savesPage = saveRepository.findByUserId(userId, pageable);
        List<ResourceSave> saves = savesPage.getContent();

        List<Resource> resources = saves.stream()
                .map(ResourceSave::getResource)
                .toList();

        List<ResourceResponse> enriched = enrichList(resources, userId);

        Map<UUID, Instant> savedAtMap = new HashMap<>();
        for (ResourceSave s : saves) {
            savedAtMap.put(s.getResource().getId(), s.getCreatedAt());
        }

        List<ResourceResponse> finalList = enriched.stream()
                .map(rr -> new ResourceResponse(
                        rr.id(),
                        rr.ownerId(),
                        rr.ownerName(),
                        rr.title(),
                        rr.description(),
                        rr.link(),
                        rr.type(),
                        rr.tags(),
                        rr.createdAt(),
                        savedAtMap.get(rr.id()),  // ✅ savedAt

                        rr.likeCount(),
                        rr.saveCount(),
                        rr.likedByMe(),
                        rr.savedByMe(),

                        rr.content(),
                        rr.coverImage(),
                        rr.fileUrl(),
                        rr.fileName(),
                        rr.fileContentType(),
                        rr.readTimeMinutes(),
                        rr.isPublished(),
                        rr.publishedAt(),
                        rr.visibility(),
                        rr.seriesSlug(),
                        rr.seriesTitle(),
                        rr.seriesPartNumber(),
                        rr.repostCount(),
                        rr.repostedByMe(),
                        rr.viewCount()
                ))
                .toList();

        return new PageResponse<>(
                finalList,
                savesPage.getNumber(),
                savesPage.getSize(),
                savesPage.getTotalElements(),
                savesPage.getTotalPages(),
                savesPage.isLast()
        );
    }

    private Integer calculateReadTime(String content) {
        if (content == null || content.isBlank()) return null;
        int wordCount = content.trim().split("\\s+").length;
        return Math.max(1, wordCount / 200);
    }
}