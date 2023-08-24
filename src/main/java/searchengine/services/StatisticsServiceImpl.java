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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


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
            Optional<searchengine.model.Site> isSiteExistInRepository = indexingService.getSitesRepository()
                    .findByUrl(site.getUrl());
            if (isSiteExistInRepository.isPresent()){
                searchengine.model.Site siteFromRepository = isSiteExistInRepository.get();
                int pages = indexingService.getPageRepository().findAllBySiteId(siteFromRepository).size();
                int lemmas = indexingService.getLemmaRepository().findAllBySiteId(siteFromRepository).size();
                total.setPages(total.getPages() + pages);
                total.setLemmas(total.getLemmas() + lemmas);
                detailed.add(getDetailedStatisticsItemIfSiteExist(siteFromRepository,pages,lemmas));
                indexingService.fillingMapCountPages(site.getUrl(),pages);
            } else {
                detailed.add(getDetailedStatisticsItemIfSiteNotExist(site));
            }
        }
        changeIndexingStatus(detailed);

        return getStatisticsResponse(getStatisticsData(detailed,total),true);
    }

    private DetailedStatisticsItem getDetailedStatisticsItemIfSiteExist(searchengine.model.Site site,
                                                                        int pages,
                                                                        int lemmas) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        item.setPages(pages);
        item.setLemmas(lemmas);
        item.setStatus(site.getStatus().name());
        item.setError(site.getLastError());
        item.setStatusTime(Timestamp.valueOf(site.getStatusTime()).getTime());

        return item;
    }

    private DetailedStatisticsItem getDetailedStatisticsItemIfSiteNotExist(Site site) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        item.setPages(0);
        item.setLemmas(0);
        item.setStatus("FAILED");
        item.setError("Не проиндексирован");
        item.setStatusTime(Timestamp.valueOf(LocalDateTime.now()).getTime());

        return item;
    }


    private void changeIndexingStatus (List<DetailedStatisticsItem> detailed) {
        Optional<DetailedStatisticsItem> optional = detailed.stream()
                                                                    .filter(d -> d.getStatus().equals("INDEXING"))
                                                                    .findAny();
        if (!optional.isPresent()){
            indexingService.setStatusIndex(false);
        }
    }

    private StatisticsResponse getStatisticsResponse (StatisticsData data, boolean result) {
        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(data);
        response.setResult(result);
        return response;
    }

    private StatisticsData getStatisticsData (List<DetailedStatisticsItem> detailed, TotalStatistics total) {
        StatisticsData data = new StatisticsData();
        if (detailed.isEmpty()){
            data.setDetailed(new ArrayList<>());
        }
        data.setDetailed(detailed);
        data.setTotal(total);

        return data;
    }

}
