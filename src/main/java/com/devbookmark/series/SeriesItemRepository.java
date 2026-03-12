package com.devbookmark.series;

import org.springframework.data.jpa.repository.JpaRepository;
import com.devbookmark.series.projection.ResourceSeriesInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeriesItemRepository extends JpaRepository<SeriesItem, UUID> {

    Optional<SeriesItem> findBySeriesIdAndResourceId(UUID seriesId, UUID resourceId);

    boolean existsBySeriesIdAndPartNumber(UUID seriesId, int partNumber);

    boolean existsBySeriesIdAndResourceId(UUID seriesId, UUID resourceId);

    int countBySeriesId(UUID seriesId);

    void deleteBySeriesIdAndResourceId(UUID seriesId, UUID resourceId);
    boolean existsByResourceId(UUID resourceId);

    @Query("""
  select 
    si.resource.id as resourceId,
    s.slug as slug,
    s.title as title,
    si.partNumber as partNumber
  from SeriesItem si
  join si.series s
  where si.resource.id in :resourceIds
""")
    List<ResourceSeriesInfo> findSeriesInfoByResourceIds(@Param("resourceIds") List<UUID> resourceIds);
}