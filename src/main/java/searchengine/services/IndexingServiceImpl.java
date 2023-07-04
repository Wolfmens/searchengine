package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Site;
import searchengine.config.SitesList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{

    private final SitesList sites;
    private final ArrayList<Thread> threads = new ArrayList<>();
    private HashSet<String> pathList = new HashSet<>();
    private volatile boolean isStatusIndex;


    @Autowired
    private SitesRepository sitesRepository;
    @Autowired
    private PageRepository pageRepository;


    @Override
    public boolean indexing() {
        if (deleteDataBySite()){
            setSitesAndPagesToRepository();
            return true;
        }
        return false;
    }

    private boolean deleteDataBySite () {
        List<Site> siteList = sitesRepository.findAll();
        siteList.forEach(s -> pageRepository.deleteBySiteId(s));
        sitesRepository.deleteAll();
        return pageRepository.findAll().size() == 0 && sitesRepository.findAll().size() == 0;
    }

    private void setSitesAndPagesToRepository() {
        sites.getSites().forEach(s -> {
            Thread thread = new BypassAndAddSitesAndPagesToRepositoryThread(sitesRepository,s,this);
            threads.add(thread);
        });
        threads.forEach(Thread::start);
    }

    @Override
    public boolean stopIndex() {
        if (isStatusIndex){
            for (Thread thread : threads){
                Thread.State state = thread.getState();
                if (!state.equals(Thread.State.TERMINATED)){
                    Thread thread1 = new Thread(new InterapterThread(thread));
                    thread1.start();
                }

            }
            setStatusIndex(false);
            threads.clear();
            return true;
        }
        return false;
    }

    public PageRepository getPageRepository() {
        return pageRepository;
    }

    public SitesRepository getSitesRepository() {
        return sitesRepository;
    }

    public void addPathToList (String path){
        pathList.add(path);
    }

    public HashSet<String> getPathList() {
        return pathList;
    }

    @Override
    public void setStatusIndex(boolean statusIndex){
        isStatusIndex = statusIndex;
    }

    @Override
    public boolean isStatusIndex() {
        return isStatusIndex;
    }
}
