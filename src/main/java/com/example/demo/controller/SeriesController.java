package com.example.demo.controller;

import com.example.demo.model.Chapter;
import com.example.demo.model.Series;
import com.example.demo.service.SeriesService;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Data
@Validated
@CrossOrigin
@RestController
@RequestMapping("api/series")
public class SeriesController {

    private final SeriesService seriesService;
    private UUID bookUuid;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Series>> getSeries(@RequestParam Map<String, ?> allParams) {
        return seriesService.getSeries(allParams);
    }

    @GetMapping(path = "/{seriesUUID}/chapters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Chapter>> getSeriesChapters(@RequestParam Map<String, ?> allParams, @PathVariable("seriesUUID") UUID seriesUUID) {
        return seriesService.getSeriesChapters(allParams, seriesUUID);
    }

}
