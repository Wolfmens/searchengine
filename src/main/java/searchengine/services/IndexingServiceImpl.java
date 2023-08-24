package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.config.SerenaSearchBot;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.config.SitesList;
import searchengine.repositories.IndexLemmaRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SitesRepository;
import searchengine.utils.BypassAndAddSitesAndPagesToRepositoryThread;
import searchengine.utils.ReturnLemmas;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final ArrayList<Thread> threads = new ArrayList<>();
    private HashSet<String> pathList = new HashSet<>();
    private volatile boolean isStatusIndex;
    private final SerenaSearchBot serenaSearchBot;
    private HashMap<String, Integer> countPagesBySite = new HashMap<>();
    private final String[] TYPES = {".png", ".jpeg", ".jpg", ".pdf"};
    private ConcurrentHashMap<Integer,HashMap<String,Set<String>>> map = new ConcurrentHashMap<>();

    @Autowired
    private SitesRepository sitesRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexLemmaRepository indexLemmaRepository;

    @Override
    public HashMap<Object,Object> indexing() {
        if (!isStatusIndex){
            setStatusIndex(true);
            if (deleteDataBySite()) {
                setSitesAndPagesToRepository();
                return new HashMap<>() {{
                    put("result", true);
                }};
            }
        }
        return new HashMap<>() {{
            put("result", false);
            put("error", "Индексация уже запущена");
        }};
    }

    private boolean deleteDataBySite() {
        indexLemmaRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        sitesRepository.deleteAll();
        return pageRepository.findAll().isEmpty() && sitesRepository.findAll().isEmpty();
    }

    private void setSitesAndPagesToRepository() {
        sites.getSites().forEach(s -> {
            Thread thread = new BypassAndAddSitesAndPagesToRepositoryThread(sitesRepository, s, this);
            threads.add(thread);
        });
        threads.forEach(Thread::start);
    }

    @Override
    public HashMap<Object,Object> stopIndex() {
        if (isStatusIndex) {
            setStatusIndex(false);
            threads.clear();
            fillingMapCountPages();

            return new HashMap<>() {{
                put("result", true);
            }};
        }
        return new HashMap<>() {{
            put("result", false);
            put("error", "Индексация не запущена");
        }};
    }

    @Override
    public HashMap<Object,Object> action(String url) {
        if (url.isBlank()) {
            return new HashMap<>() {{
                put("result", false);
                put("error", "Ваш запрос пустой");
            }};
        }
        try {
            Document document = Jsoup.connect(url).get();
            String content = Jsoup.connect(url).get().toString();
            int statusCode = document.connection().response().statusCode();
            if (hasSite(url) && isConnectUrl(statusCode)) {
                addOrUpdatePageToRepository(url, statusCode, content);
                return new HashMap<>() {{put("result", true);}};
            } else {
                return new HashMap<>() {{
                    put("result", false);
                    put("error", "Данная страница находится за пределами сайтов," +
                            " указанных в конфигурационном файле");
                }};
            }
        } catch (Exception ex) {
            System.err.println(ex.getClass() + "---" + ex.getMessage());
        }
        return new HashMap<>() {{
            put("result", false);
            put("error", "Возникла ошибка. повторите пожалуйста повторите запрос позже");
        }};
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
                fillingMapCountPages();
            }
        } catch (Exception e) {
            System.err.println(e.getClass() + "---" + e.getMessage());
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

    @Override
    public HashMap<String, Integer> getCountPagesBySite() {
        if (countPagesBySite.isEmpty()) {
            fillingMapCountPages();
        } else {
            return countPagesBySite;
        }
        return countPagesBySite;
    }

    public void fillingMapCountPages() {
        countPagesBySite.clear();
        List<Site> siteList = sitesRepository.findAll();
        siteList.forEach(s -> {
            int count = pageRepository.findAllBySiteId(s).size();
            countPagesBySite.put(s.getUrl(), count);
        });
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

    @Override
    public String[] getTypes() {
        return TYPES;
    }

    @Override
    public void fillingMapCountPages(String urlSite, int countPages) {
        countPagesBySite.put(urlSite,countPages);
    }

    @Override
    public ConcurrentHashMap<Integer, HashMap<String, Set<String>>> getMap() {
        return map;
    }

    @Override
    public void fillingMap(Integer id, HashMap<String, Set<String>> mapWordOfFound) {
        map.put(id,mapWordOfFound);
    }
}

