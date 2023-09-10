package searchengine.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.NodeWebsite;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Type;
import searchengine.services.IndexingService;

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
            Document document = getDocumentAndWorkWithRepositories(url,page);
            if (isStatusCodeGood(page)){
                return indexingService.isStatusIndex();
            }
            Elements elements = document.select("a");
            for (Element element : elements) {
                String urlOfElement = element.attr("abs:href");
                if (hasHttpProtocolUrlAndHeadURL(urlOfElement)) {
                    if (hasLinksInRootNodeAndPathInGeneralListAndNotURLByElementsWebsite(urlOfElement)) {
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
            subTask.forEach(BypassSites::join);
        } catch (Error er) {
            settingErrorStatusBySiteInRepository(er.getMessage());
        } catch (Exception ex) {
            System.err.println(ex.getClass() + " --- " + ex.getMessage());
        }
        return indexingService.isStatusIndex();
    }

    private void settingErrorStatusBySiteInRepository (String error){
        Site site = nodeWebsite.getSite();
        site.setStatus(Type.FAILED);
        site.setLastError(error);
        indexingService.getSitesRepository().save(site);
    }

    private boolean hasLinksInRootNodeAndPathInGeneralListAndNotURLByElementsWebsite (String url) {
        boolean hasNotURLByElementsWebsite = url.matches(nodeWebsite.getSite().getUrl() + ".*#.*");
        boolean hasLinksInRootNode = nodeWebsite.getLinks().contains(url);
        boolean hasPathInGeneralList = indexingService.getPathList().contains(url);

        return hasLinksInRootNode ||
               hasPathInGeneralList ||
               hasNotURLByElementsWebsite ||
               ApplicationConstantsAndChecks.hasUrlIsNotImage(url);
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

    private void setFieldsOfPage(Document doc, String url, Page page) {
        int statusCode = doc.connection().response().statusCode();
        page.setContent(doc.toString());
        page.setPath(url);
        page.setSiteId(nodeWebsite.getSite());
        page.setCode(statusCode);
    }

    private void addPageToRepositoryIfBypassStop(String url) throws Exception {
        Page page = new Page();
        if (!ApplicationConstantsAndChecks.hasUrlIsNotImage(url)) {
            getDocumentAndWorkWithRepositories(url, page);
        }
    }

    private Document getDocumentAndWorkWithRepositories (String url, Page page) throws Exception {
        Connection connection = Jsoup.connect(url).timeout(200000)
                .userAgent(indexingService.getSerenaSearchBot().getUserAgent())
                .referrer(indexingService.getSerenaSearchBot().getReferrer()).ignoreHttpErrors(true);
        Document document = connection.get();
        setFieldsOfPage(document, url, page);
        Optional<Page> optional = indexingService.getPageRepository().findByPath(url);
        if (!optional.isPresent()) {
            saveToRepository(page);
            if (!isStatusCodeGood(page)) {
                transformationWebHtmlInLemmas(document, page);
            }
        }
        indexingService.addPathToList(url);

        return document;
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
