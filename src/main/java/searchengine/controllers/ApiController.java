package searchengine.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService,
                         IndexingService indexingService,
                         SearchService searchService) {

        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping(value = "/startIndexing", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> indexing() {
        return ResponseEntity.ok(indexingService.indexing());
    }


    @GetMapping(value = "/stopIndexing", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndex());
    }

    @PostMapping(value = "/indexPage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addOrUpdateSeparatePage(@RequestParam String url) {
        return ResponseEntity.ok(indexingService.action(url));
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> search(@RequestParam(required = false) String query,
                                         @RequestParam(required = false) String site,
                                         @RequestParam Integer offset,
                                         @RequestParam Integer limit) {
        return ResponseEntity.ok().body(searchService.getSearchResponse(query, site, offset, limit));
    }


}
