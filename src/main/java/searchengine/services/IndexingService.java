package searchengine.services;

import searchengine.config.SerenaSearchBot;

import java.util.Collection;
import java.util.HashMap;


public interface IndexingService {

    boolean indexing ();
    void setStatusIndex(boolean statusIndex);
    boolean isStatusIndex();
    boolean stopIndex();
    PageRepository getPageRepository();
    SitesRepository getSitesRepository();
    void addPathToList(String url);
    Collection<String> getPathList();
    SerenaSearchBot getSerenaSearchBot();
    boolean action(String url);
    LemmaRepository getLemmaRepository();
    IndexLemmaRepository getIndexLemmaRepository();
    HashMap<String,Integer> getCountPagesBySite();
    String[] getTypes();
    void fillingMapCountPages();
    void fillingMapCountPages(String urlSite, int countPages);
}
