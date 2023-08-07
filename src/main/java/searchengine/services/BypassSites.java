package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.NodeWebsite;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Type;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.concurrent.RecursiveTask;


public class BypassSites extends RecursiveTask<Boolean> {

    private NodeWebsite nodeWebsite;
    private String url;
    private IndexingService indexingService;
    private Page page;

    public BypassSites(NodeWebsite nodeWebsite, IndexingService indexingService) {
        page = new Page();
        this.indexingService = indexingService;
        this.nodeWebsite = nodeWebsite;
        url = nodeWebsite.getWebsite();
    }

    @Override
    protected Boolean compute() {
        try {
            Thread.sleep(500);
            LinkedHashSet<BypassSites> subTask = new LinkedHashSet<>();
            Document document = Jsoup.connect(url).timeout(200000)
                    .userAgent(indexingService.getSerenaSearchBot().getUserAgent())
                    .referrer(indexingService.getSerenaSearchBot().getReferrer())
                    .get();
            setFieldsOfPage(document,url, page);
                Elements elements = document.select("a");
                Optional<Page> optional = indexingService.getPageRepository().findByPath(url);
                if (!optional.isPresent()) {
                    saveToRepository(page);
                    if (!isStatusCodeGood(page)) {
                        transformationWebHtmlInLemmas(document, page);
                    }
                }
                indexingService.addPathToList(url);
                for (Element element : elements) {
                    String urlOfElement = element.attr("abs:href");
                    if (hasHttpProtocolUrlAndHeadURL(urlOfElement)) {
                        if (hasLinksInRootNodeAndPathInGeneralListAndNotURLByElementsWebsite (urlOfElement)) {
                            continue;
                        }
                        if (!indexingService.isStatusIndex()) {
                            addPageToRepositoryIfBypassStop(urlOfElement);
                            System.err.println(urlOfElement + " - " + " завершено");
                        } else {
                            forkBypass(urlOfElement, subTask);
                        }
                    }
                }
                for (BypassSites bypass : subTask) {
                    bypass.join();
                }
        } catch (Error er){
            Site site = nodeWebsite.getSite();
            String error = er.getMessage();
            settingErrorStatusBySiteInRepository(site,error);
        } catch (Exception ex){
            System.err.println(ex.getClass() + " --- " + ex.getMessage());
        }
        return indexingService.isStatusIndex();
    }

    private void settingErrorStatusBySiteInRepository (Site site, String error){
        site.setStatus(Type.FAILED);
        site.setLastError(error);
        indexingService.getSitesRepository().save(site);
    }

    private boolean hasLinksInRootNodeAndPathInGeneralListAndNotURLByElementsWebsite (String url) {
        boolean hasNotURLByElementsWebsite = url.matches(nodeWebsite.getSite().getUrl() + ".*#.*");
        boolean hasLinksInRootNode = nodeWebsite.getLinks().contains(url);
        boolean hasPathInGeneralList = indexingService.getPathList().contains(url);

        return hasLinksInRootNode || hasPathInGeneralList || hasNotURLByElementsWebsite;
    }

    private boolean hasHttpProtocolUrlAndHeadURL (String url) {
        boolean hasHeadURL = url.matches(nodeWebsite.getSite().getUrl() + "/.+");
        boolean hasHttpProtocolUrl = url.matches("http.*");
        return hasHttpProtocolUrl && hasHeadURL;
    }

    private boolean isStatusCodeGood(Page page){
        String statusCode = String.valueOf(page.getCode());
        return statusCode.startsWith("4") || statusCode.startsWith("5");
    }

    private void setFieldsOfPage(Document doc, String url, Page page) throws Exception {
        int statusCode = doc.connection().response().statusCode();
        page.setContent(doc.toString());
        page.setPath(url);
        page.setSiteId(nodeWebsite.getSite());
        page.setCode(statusCode);
    }

    private void addPageToRepositoryIfBypassStop(String url) throws Exception {
        Document document = Jsoup.connect(url).timeout(200000)
                .userAgent(indexingService.getSerenaSearchBot().getUserAgent())
                .referrer(indexingService.getSerenaSearchBot().getReferrer())
                .get();
        Page page = new Page();
        setFieldsOfPage(document, url, page);
        Optional<Page> optional = indexingService.getPageRepository().findByPath(url);
        if (!optional.isPresent()) {
           saveToRepository(page);
           transformationWebHtmlInLemmas(document, page);
        }
        indexingService.addPathToList(url);
    }

    private void forkBypass (String url,  LinkedHashSet<BypassSites> subTask){
        NodeWebsite child = new NodeWebsite(url, nodeWebsite.getSite());
        BypassSites bypassTask = new BypassSites(child, indexingService);
        bypassTask.fork();
        subTask.add(bypassTask);
        nodeWebsite.addLink(url, child);
    }

    private void saveToRepository(Page page){
        indexingService.getPageRepository().save(page);
        nodeWebsite.getSite().setStatusTime(LocalDateTime.now());
        indexingService.getSitesRepository().save(nodeWebsite.getSite());
    }

    private void transformationWebHtmlInLemmas (Document doc, Page page) throws Exception {
            ReturnLemmas returnLemmas = new ReturnLemmas(indexingService, nodeWebsite.getSite(), page);
            returnLemmas.getLemmas(doc.toString());
    }
}
