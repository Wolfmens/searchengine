package searchengine.services;

import searchengine.config.SerenaSearchBot;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
}
