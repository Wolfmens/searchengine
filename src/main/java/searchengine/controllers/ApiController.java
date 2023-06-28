package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;


    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping(value = "/startIndexing", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> indexing() {
        if (!indexingService.isStatusIndex()){
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
        if (indexingService.isStatusIndex()){
            if (indexingService.stopIndex()){
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

}
