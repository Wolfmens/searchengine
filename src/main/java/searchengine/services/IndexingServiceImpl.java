package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SerenaSearchBot;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.config.SitesList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final ArrayList<Thread> threads = new ArrayList<>();
    private HashSet<String> pathList = new HashSet<>();
    private volatile boolean isStatusIndex;
    private final SerenaSearchBot serenaSearchBot;

    @Autowired
    private SitesRepository sitesRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexLemmaRepository indexLemmaRepository;


    @Override
    public boolean indexing() {
        if (deleteDataBySite()) {
            setSitesAndPagesToRepository();
            return true;
        }
        return false;
    }

    private boolean deleteDataBySite() {
        List<Site> siteList = sitesRepository.findAll();
//        siteList.forEach(s -> {
//            pageRepository.findAll().forEach(p -> {
//                indexLemmaRepository.deleteByPageId(p);
//            });
//            pageRepository.deleteBySiteId(s);
//            lemmaRepository.deleteBySiteId(s);
//        });
        indexLemmaRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        sitesRepository.deleteAll();
        return pageRepository.findAll().size() == 0 && sitesRepository.findAll().size() == 0;
    }

    private void setSitesAndPagesToRepository() {
        sites.getSites().forEach(s -> {
            Thread thread = new BypassAndAddSitesAndPagesToRepositoryThread(sitesRepository, s, this);
            threads.add(thread);
        });
        threads.forEach(Thread::start);
    }

    @Override
    public boolean stopIndex() {
        if (isStatusIndex) {
            setStatusIndex(false);
            threads.clear();
            return true;
        }
        return false;
    }

    @Override
    public boolean action(String url) {
        if (url.isBlank()) {
            return false;
        }
        try {
            Document document = Jsoup.connect(url).get();
            String content = Jsoup.connect(url).get().toString();
            int statusCode = document.connection().response().statusCode();
            if (hasSite(url) && isConnectUrl(statusCode)) {
                addOrUpdatePageToRepository(url, statusCode, content);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private String getSiteUrlForReturnFromRepository(String url) {
        String[] urlElements = url.split("/");
        String siteUrl = "";
        for (int i = 0; i < 3; i++) {
            siteUrl = siteUrl.concat(urlElements[i] + "/");
        }
        return siteUrl.substring(0, siteUrl.length() - 1);
    }

    private void addOrUpdatePageToRepository(String url, int statusCode, String content) {
        try {
            Site site = sitesRepository.findByUrl(getSiteUrlForReturnFromRepository(url)).get();
            Optional<Page> pageFromRepository = pageRepository.findByPath(url);
            if (pageFromRepository.isPresent()) {
                ReturnLemmas returnLemmas = new ReturnLemmas(this, site, pageFromRepository.get());
                returnLemmas.updateLemmas(content);
            } else {
                Page page = new Page();
                page.setCode(statusCode);
                page.setPath(url);
                page.setContent(content);
                page.setSiteId(site);
                pageRepository.save(page);
                ReturnLemmas returnLemmas = new ReturnLemmas(this, site, page);
                returnLemmas.getLemmas(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasSite(String url) {
        for (searchengine.config.Site sitesUrl : sites.getSites()) {
            if (url.contains(sitesUrl.getUrl())) {
                return true;
            }
        }
        return false;
    }

    private boolean isConnectUrl(int statusCode) {
        return statusCode != 404;
    }

    public PageRepository getPageRepository() {
        return pageRepository;
    }

    public SitesRepository getSitesRepository() {
        return sitesRepository;
    }

    public void addPathToList(String path) {
        pathList.add(path);
    }

    public HashSet<String> getPathList() {
        return pathList;
    }

    @Override
    public void setStatusIndex(boolean statusIndex) {
        isStatusIndex = statusIndex;
    }

    @Override
    public boolean isStatusIndex() {
        return isStatusIndex;
    }

    @Override
    public SerenaSearchBot getSerenaSearchBot() {
        return serenaSearchBot;
    }

    @Override
    public LemmaRepository getLemmaRepository() {
        return lemmaRepository;
    }

    @Override
    public IndexLemmaRepository getIndexLemmaRepository() {
        return indexLemmaRepository;
    }
}

