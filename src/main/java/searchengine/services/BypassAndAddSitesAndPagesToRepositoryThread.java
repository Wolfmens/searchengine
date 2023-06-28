package searchengine.services;

import searchengine.model.NodeWebsite;
import searchengine.model.Site;
import searchengine.model.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class BypassAndAddSitesAndPagesToRepositoryThread extends Thread {

    private searchengine.config.Site siteConfig;
    private SitesRepository repository;
    private IndexingService indexingService;

    public BypassAndAddSitesAndPagesToRepositoryThread(SitesRepository sitesRepository,
                                                       searchengine.config.Site site,
                                                       IndexingService indexingService){
        this.siteConfig = site;
        repository = sitesRepository;
        this.indexingService = indexingService;
    }

    @Override
    public void run() {
            while (!isInterrupted()){
                Site site = new Site();
                site.setUrl(siteConfig.getUrl());
                site.setStatus(Type.INDEXING);
                site.setName(siteConfig.getName());
                site.setStatusTime(LocalDateTime.now());
                repository.save(site);
                bypassSitesAndSetPagesToRepository(site);
                repository.save(site);
                break;
            }
    }

    private void bypassSitesAndSetPagesToRepository(Site site){
        String url = site.getUrl();
        NodeWebsite root = new NodeWebsite(url,site);
        BypassSites bypassSites = new BypassSites(root,indexingService);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(bypassSites);
        site.setStatus(Type.INDEXED);
        site.setStatusTime(LocalDateTime.now());
    }
}
