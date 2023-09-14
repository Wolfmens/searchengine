package searchengine.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import searchengine.model.NodeWebsite;
import searchengine.model.Site;
import searchengine.model.Type;
import searchengine.repositories.SitesRepository;
import searchengine.services.IndexingService;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

public class BypassAndAddSitesAndPagesToRepositoryThread extends Thread {

    private final searchengine.config.Site siteConfig;
    private final SitesRepository repository;
    private final IndexingService indexingService;

    public BypassAndAddSitesAndPagesToRepositoryThread(SitesRepository sitesRepository,
                                                       searchengine.config.Site site,
                                                       IndexingService indexingService) {
        this.siteConfig = site;
        repository = sitesRepository;
        this.indexingService = indexingService;
    }

    @Override
    public void run() {
        if (isCheckUrl(siteConfig.getUrl())) {
            createSiteEntityIf404();
        } else {
            createSiteEntityIf200();
        }
    }

    private void createSiteEntityIf200() {
        Site site = new Site();
        site.setUrl(siteConfig.getUrl());
        site.setStatus(Type.INDEXING);
        site.setName(siteConfig.getName());
        site.setStatusTime(LocalDateTime.now());
        repository.save(site);
        bypassSitesAndSetPagesToRepository(site);
        repository.save(site);
    }

    private void createSiteEntityIf404() {
        Site site = new Site();
        site.setUrl(siteConfig.getUrl());
        site.setStatus(Type.FAILED);
        site.setName(siteConfig.getName());
        site.setStatusTime(LocalDateTime.now());
        site.setLastError("Страница не найдена");
        repository.save(site);
    }

    private boolean isCheckUrl(String url) {
        try {
            Connection connection = Jsoup.connect(url).ignoreHttpErrors(true);
            int statusCode = connection.execute().statusCode();
            if (statusCode == 404) {
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return false;
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
        }
    }
}

