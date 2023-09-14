package searchengine.services;

import searchengine.config.SerenaSearchBot;
import searchengine.repositories.IndexLemmaRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SitesRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public interface IndexingService {

    HashMap<Object, Object> indexing();

    void setStatusIndex(boolean statusIndex);

    boolean isStatusIndex();

    HashMap<Object, Object> stopIndex();

    PageRepository getPageRepository();

    SitesRepository getSitesRepository();

    void addPathToList(String url);

    Collection<String> getPathList();

    SerenaSearchBot getSerenaSearchBot();

    HashMap<Object, Object> action(String url);

    LemmaRepository getLemmaRepository();

    IndexLemmaRepository getIndexLemmaRepository();

    HashMap<String, Integer> getCountPagesBySite();

    void fillingMapCountPages();

    void fillingMapCountPages(String urlSite, int countPages);

    ConcurrentHashMap<Integer, HashMap<String, Set<String>>> getMapOfPositionWordsByPage();

    void fillingMap(Integer id, HashMap<String, Set<String>> mapWordOfFound);
}
