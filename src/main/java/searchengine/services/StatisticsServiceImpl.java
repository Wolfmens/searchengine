package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final IndexingService indexingService;
    private final SitesList sites;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = (int) indexingService.getPageRepository().count();
            int lemmas = (int) indexingService.getLemmaRepository().count();
            item.setPages(pages);
            item.setLemmas(lemmas);
            searchengine.model.Site siteFromRepository = indexingService.getSitesRepository()
                                                         .findByUrl(site.getUrl())
                                                         .get();
            String statusSite = siteFromRepository.getStatus().name();
            item.setStatus(statusSite);
            String error = siteFromRepository.getLastError();
            item.setError(error);
            item.setStatusTime(Timestamp.valueOf(siteFromRepository.getStatusTime()).getTime());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setDetailed(detailed);
        data.setTotal(total);

        response.setStatistics(data);
        response.setResult(true);

        return response;
    }
}
