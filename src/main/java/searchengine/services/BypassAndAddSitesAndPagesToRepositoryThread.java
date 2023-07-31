package searchengine.services;

import searchengine.model.NodeWebsite;
import searchengine.model.Site;
import searchengine.model.Type;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

public class BypassAndAddSitesAndPagesToRepositoryThread extends Thread {

    private searchengine.config.Site siteConfig;
    private SitesRepository repository;
    private IndexingService indexingService;

    public BypassAndAddSitesAndPagesToRepositoryThread(SitesRepository sitesRepository,
                                                       searchengine.config.Site site,
                                                       IndexingService indexingService) {
        this.siteConfig = site;
        repository = sitesRepository;
        this.indexingService = indexingService;
    }

    @Override
    public void run() {
            Site site = new Site();
            site.setUrl(siteConfig.getUrl());
            site.setStatus(Type.INDEXING);
            site.setName(siteConfig.getName());
            site.setStatusTime(LocalDateTime.now());
            repository.save(site);
            bypassSitesAndSetPagesToRepository(site);
            repository.save(site);
    }

    private void bypassSitesAndSetPagesToRepository(Site site) {
        String url = site.getUrl();
        NodeWebsite root = new NodeWebsite(url, site);
        BypassSites bypassSites = new BypassSites(root, indexingService);
        ForkJoinPool pool = new ForkJoinPool();
        boolean isEndIndexProcess = pool.invoke(bypassSites);
        if (!isEndIndexProcess) {
            site.setStatus(Type.FAILED);
            site.setStatusTime(LocalDateTime.now());
            site.setLastError("Индексация остановлена пользователем");
            indexingService.getPathList().clear();
        } else {
            site.setStatus(Type.INDEXED);
            site.setStatusTime(LocalDateTime.now());
//            indexingService.setStatusIndex(false);   что бы заново потом можно было индексировать после индексации
        }
    }
}
