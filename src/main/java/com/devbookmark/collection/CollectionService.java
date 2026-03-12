package com.devbookmark.collection;

import com.devbookmark.collection.dto.CollectionCreateRequest;
import com.devbookmark.collection.dto.CollectionDetailResponse;
import com.devbookmark.collection.dto.CollectionSummaryResponse;
import com.devbookmark.resource.Resource;
import com.devbookmark.resource.ResourceRepository;
import com.devbookmark.resource.ResourceTag;
import com.devbookmark.resource.ResourceVisibility;
import com.devbookmark.tag.Tag;
import com.devbookmark.user.User;
import com.devbookmark.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devbookmark.collection.dto.CollectionUpdateRequest;
import java.util.*;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionResourceRepository collectionResourceRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public CollectionService(CollectionRepository collectionRepository,
                             CollectionResourceRepository collectionResourceRepository,
                             UserRepository userRepository,
                             ResourceRepository resourceRepository) {
        this.collectionRepository = collectionRepository;
        this.collectionResourceRepository = collectionResourceRepository;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public CollectionSummaryResponse create(UUID ownerId, CollectionCreateRequest req) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Collection c = Collection.builder()
                .owner(owner)
                .name(req.name().trim())
                .description(req.description() == null ? null : req.description().trim())
                .isPublic(req.isPublic() != null ? req.isPublic() : true)
                .build();

        Collection saved = collectionRepository.save(c);
        collectionRepository.flush(); // add this line

        return new CollectionSummaryResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.isPublic(),
                0,
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<CollectionSummaryResponse> myCollections(UUID ownerId) {
        List<Collection> list = collectionRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);

        List<CollectionSummaryResponse> out = new ArrayList<>();
        for (Collection c : list) {
            long count = collectionResourceRepository.countByCollectionId(c.getId());
            out.add(new CollectionSummaryResponse(
                    c.getId(),
                    c.getName(),
                    c.getDescription(),
                    c.isPublic(),
                    count,
                    c.getCreatedAt()
            ));
        }
        return out;
    }

    @Transactional
    public void addResource(UUID ownerId, UUID collectionId, UUID resourceId) {
        Collection c = collectionRepository.findByIdAndOwnerId(collectionId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found (or not owned by you)."));

        Resource r = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found."));

        boolean exists = collectionResourceRepository.findByCollectionIdAndResourceId(collectionId, resourceId).isPresent();
        if (exists) return; // idempotent

        collectionResourceRepository.save(
                CollectionResource.builder()
                        .collection(c)
                        .resource(r)
                        .build()
        );
    }

    @Transactional
    public void removeResource(UUID ownerId, UUID collectionId, UUID resourceId) {
        // only owner can remove
        collectionRepository.findByIdAndOwnerId(collectionId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found (or not owned by you)."));

        CollectionResource cr = collectionResourceRepository.findByCollectionIdAndResourceId(collectionId, resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found in collection."));

        collectionResourceRepository.delete(cr);
    }

    @Transactional(readOnly = true)
    public CollectionDetailResponse getDetail(UUID viewerIdOrNull, UUID collectionId) {
        Collection c = collectionRepository.findDetailById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found."));

        boolean isOwner = viewerIdOrNull != null && c.getOwner().getId().equals(viewerIdOrNull);
        if (!c.isPublic() && !isOwner) {
            throw new IllegalArgumentException("This collection is private.");
        }

        // build items - ✅ FIXED: Now filters resources by visibility
        List<CollectionDetailResponse.Item> items = new ArrayList<>();

        for (CollectionResource cr : c.getItems()) {
            Resource r = cr.getResource();

            // ✅ FIXED: Skip resources that viewer shouldn't see
            if (!canViewResource(r, viewerIdOrNull, isOwner)) {
                continue;
            }

            Set<String> tags = new LinkedHashSet<>();
            for (ResourceTag rt : r.getResourceTags()) {
                Tag tag = rt.getTag();
                tags.add(tag.getName());
            }

            items.add(new CollectionDetailResponse.Item(
                    r.getId(),
                    r.getTitle(),
                    r.getDescription(),
                    r.getLink(),
                    r.getType(),
                    tags,
                    cr.getAddedAt()
            ));
        }

        // sort by addedAt desc
        items.sort(Comparator.comparing(CollectionDetailResponse.Item::addedAt).reversed());

        return new CollectionDetailResponse(
                c.getId(),
                c.getOwner().getId(),
                c.getOwner().getName(),
                c.getName(),
                c.getDescription(),
                c.isPublic(),
                c.getCreatedAt(),
                items
        );
    }

    /**
     * ✅ NEW: Determines if viewer can see a resource based on visibility rules
     */
    private boolean canViewResource(Resource resource, UUID viewerId, boolean isCollectionOwner) {
        // Collection owner can see all their resources
        if (isCollectionOwner) {
            return true;
        }

        // PUBLIC resources visible to all
        if (resource.getVisibility() == ResourceVisibility.PUBLIC) {
            return true;
        }

        // PRIVATE resources only visible to owner
        if (resource.getVisibility() == ResourceVisibility.PRIVATE) {
            return false;
        }

        // FOLLOWERS resources only visible to logged-in users
        // (in future: check if viewerId follows resource owner)
        if (resource.getVisibility() == ResourceVisibility.FOLLOWERS) {
            return viewerId != null;
        }

        return false;
    }

    @Transactional
    public CollectionSummaryResponse update(UUID ownerId, UUID collectionId, CollectionUpdateRequest req) {
        Collection c = collectionRepository.findByIdAndOwnerId(collectionId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found (or not owned by you)."));

        c.setName(req.name().trim());
        c.setDescription(req.description() == null ? null : req.description().trim());
        c.setPublic(req.isPublic() != null ? req.isPublic() : true);

        // count items
        long count = collectionResourceRepository.countByCollectionId(c.getId());

        return new CollectionSummaryResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.isPublic(),
                count,
                c.getCreatedAt()
        );
    }


    @Transactional
    public void delete(UUID ownerId, UUID collectionId) {
        Collection c = collectionRepository.findByIdAndOwnerId(collectionId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found (or not owned by you)."));

        collectionRepository.delete(c);
    }
}