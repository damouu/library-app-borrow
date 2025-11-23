package com.example.demo.repository;

import com.example.demo.model.Chapter;
import com.example.demo.model.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Integer>, JpaSpecificationExecutor<Series> {

    Page<Series> findAll(Specification<Series> specification, Pageable pageable);

    @Query("SELECT b FROM chapter b WHERE b.series = :series ORDER BY b.createdAT ASC ")
    List<Chapter> findRecentChapterBySeries(@Param("series") Series series, Pageable pageable);

    @Query("SELECT b FROM series b WHERE b.seriesUUID = :seriesUUID and b.deleted_at is null ORDER BY b.createdAt desc ")
    Series findSeriesBySeries_uuid(UUID seriesUUID);

}
