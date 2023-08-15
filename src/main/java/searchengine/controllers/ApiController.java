package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.*;

import java.util.Map;

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
        if (!indexingService.isStatusIndex()) {
            indexingService.setStatusIndex(true);
            if (indexingService.indexing()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of("result", true));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("result", false));
            }
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("result", false,
                            "error", "Индексация уже запущена"));
        }
    }

    @GetMapping(value = "/stopIndexing", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> stopIndexing() {
        if (indexingService.isStatusIndex()) {
            if (indexingService.stopIndex()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(Map.of("result", true));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("result", false));
            }
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("result", false,
                            "error", "Индексация не запущена"));
        }
    }

    @PostMapping(value = "/indexPage", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addOrUpdateSeparatePage(@RequestParam String url) {
        if (indexingService.action(url)) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("result", true));
        } else {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("result", false,
                            "error", "Данная страница находится за пределами сайтов, " +
                                    "указанных в конфигурационном файле"));
        }
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> search (@RequestParam(required = false) String query,
                                                  @RequestParam(required = false) String site,
                                                  @RequestParam Integer offset,
                                                  @RequestParam Integer limit){
        return ResponseEntity.ok().body(searchService.getSearchResponse(query, site, offset, limit));
    }



}
