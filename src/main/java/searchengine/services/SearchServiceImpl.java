package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.search.DataSearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.model.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final IndexingService indexingService;
    private final String[] SERVICE_FORMS = {"ПРЕДЛ", "СОЮЗ", "МЕЖД"};
    private List<String> listLemmas;
    private final float COEFF_FREQUENCY_LEMMA_ON_PAGES = 0.8F;
    private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#####");
    private List<Page> pageList = new ArrayList<>();
    private SearchResponse searchResponse = new SearchResponse();
    private final LuceneMorphology luceneMorphology;

    @Override
    public Object getSearchResponse(String query, String site, Integer offset, Integer limit) {
        if (query.isEmpty()) {
            SearchResponse searchResponseEmpty = new SearchResponse();
            searchResponseEmpty.setCount(0);
            searchResponseEmpty.setResult(true);
            searchResponseEmpty.setData(new ArrayList<>());
            return searchResponseEmpty;
        }
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = 20;
        }
        listLemmas = getFilteredAndSortedListWithLemmas(query, site);
        if (listLemmas.isEmpty()) {
            SearchResponse searchResponseEmpty = new SearchResponse();
            searchResponseEmpty.setCount(0);
            searchResponseEmpty.setResult(true);
            searchResponseEmpty.setData(new ArrayList<>());
            return searchResponseEmpty;
        }

        if (site == null) {
            for (Site siteFromRepository : indexingService.getSitesRepository().findAll()) {
                List<String> listLemmasFilter = listLemmas.stream()
                                .filter(l -> indexingService.getLemmaRepository()
                                .findByLemma(l).get(0).getSiteId().getId() == siteFromRepository.getId())
                                .toList();
                if(!listLemmasFilter.isEmpty()){
                    pageList.addAll(getFilterPageList(siteFromRepository.getUrl(), listLemmasFilter));
                }
            }
        } else {
            pageList.addAll(getFilterPageList(site, listLemmas));
        }

        HashMap<Page, Double> mapPageByOtnRelevance = getMapPageByOtnRelevance(pageList,limit,offset);
        List<DataSearchItem> listData = getListObjectsBySearch(mapPageByOtnRelevance, listLemmas, query);
        searchResponse.setResult(true);
        searchResponse.setData(listData);
        searchResponse.setCount(listData.size());

        return searchResponse;
    }

    private HashMap<Page, Double> getMapPageByOtnRelevance(List<Page> pageList, int limit, int offset) {
        HashMap<Page, Integer> mapPageByAbsRelevance = new HashMap<>();
        HashMap<Page, Double> mapPageByOtnRelevance = new HashMap<>();
        for (Page page : pageList) {
            int absRel = 0;
            for (String lemma : listLemmas) {
                Lemma lemmaFromRepository = indexingService.getLemmaRepository().findByLemma(lemma).get(0);
                Optional<Index> optionalIndex = indexingService.getIndexLemmaRepository()
                        .findByLemmaIdAndPageId(lemmaFromRepository, page);
                if (optionalIndex.isPresent()) {
                    absRel += optionalIndex.get().getRank();
                }
            }
            mapPageByAbsRelevance.put(page, absRel);
        }
        int maxAbsRelevance = mapPageByAbsRelevance.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getValue();
        for (Map.Entry<Page, Integer> entry : mapPageByAbsRelevance.entrySet()) {
            double otnRel = (double) entry.getValue() / maxAbsRelevance;
            mapPageByOtnRelevance.put(entry.getKey(), otnRel);
        }
        return mapPageByOtnRelevance.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .skip(offset)
                .limit(limit)
                .collect(LinkedHashMap::new, (m, c) -> m.put(c.getKey(), c.getValue()), LinkedHashMap::putAll);
    }

    private List<Page> getFilterPageList(String site, List<String> listLemmas) {
        Site siteFromRepository = indexingService.getSitesRepository().findByUrl(site).get();
        Lemma rareLemmaFromRepository = indexingService.getLemmaRepository().findByLemma(listLemmas.get(0)).get(0);
        List<Index> listOfIndexesFromRepositoryByRareLemma = indexingService
                .getIndexLemmaRepository()
                .findAllByLemmaId(rareLemmaFromRepository);
        List<Page> pageList = listOfIndexesFromRepositoryByRareLemma.stream()
                .filter(i -> i.getPageId().getSiteId().getId() == siteFromRepository.getId())
                .map(Index::getPageId)
                .toList();
        if (pageList.isEmpty()) {
            return pageList;
        }
        Iterator<Page> iterator = pageList.iterator();

        for (int i = 1; i < listLemmas.size(); i++) {
            Lemma lemma = indexingService.getLemmaRepository().findByLemma(listLemmas.get(i)).get(0);
            if (lemma.getSiteId().getId() != siteFromRepository.getId()) {
                continue;
            }
            List<Index> listOfIndexesFromRepositoryByLemma = indexingService
                    .getIndexLemmaRepository()
                    .findAllByLemmaId(lemma);
            while (iterator.hasNext()) {
                Page page = iterator.next();
                Optional<Index> optional =
                        listOfIndexesFromRepositoryByLemma.stream()
                                .filter(in -> in.getPageId().getId() == page.getId())
                                .findAny();
                if (!optional.isPresent() && !optional.isEmpty()) {
                    iterator.remove();
                }
            }
        }
        return pageList;
    }

    private ResponseEntity getResponseIfError(String response) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("result", false,
                        "error", response));
    }

    private String creationSnippet(String path, String lemma, String query) {
        String[] queryElements = query.split(" ");
        StringBuilder finalSnippet = new StringBuilder();
        HashSet<String> setSnippets = new HashSet<>();
        try {
            String text = indexingService.getPageRepository().findByPath(path).get().getContent();
            for (String word : queryElements) {
                String lemmaFromQuery = luceneMorphology.getNormalForms(word.toLowerCase()).get(0).strip();
                if (!lemma.contains(lemmaFromQuery)) {
                   continue;
                }
                Pattern pattern = Pattern.compile("[А-Яа-я\\s]*\\s*" + word + "\\s*[А-Яа-я\\s]*");
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    String initialSnippet = matcher.group();
                    String highlightedWordFromQuery = "<b>" + word + "</b>";
                    setSnippets.add(initialSnippet.replace(word, highlightedWordFromQuery).strip() + "...");
                }
            }
            for (String word : setSnippets) {
                if (finalSnippet.length() > 200) {
                    return finalSnippet.toString();
                }
                finalSnippet.append(word);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return finalSnippet.toString();
    }

    private List<DataSearchItem> getListObjectsBySearch(Map<Page, Double> map, List<String> lemmas, String query) {
        List<DataSearchItem> list = new ArrayList<>();

        for (Map.Entry<Page, Double> entry : map.entrySet()) {
            Page page = entry.getKey();
            StringBuilder builder = new StringBuilder();
            for (String lemma : lemmas) {
                builder.append(creationSnippet(page.getPath(), lemma, query));
                builder.append("\n");
            }
            if (builder.toString().isBlank()){
                continue;
            }
            Site site = page.getSiteId();
            String replaceSign = DECIMAL_FORMAT.format(entry.getValue()).replace(",", ".");
            double otnRel = Double.parseDouble(replaceSign);
            String uri = getUri(page.getPath(),site.getUrl());
            try {
                Document document = Jsoup.connect(page.getPath()).get();
                String title = document.title();
                String snippet = builder.toString().strip();
                list.add(new DataSearchItem(site.getUrl(),site.getName(),uri,title,snippet,otnRel));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private String getUri (String path, String siteUrl){
        return path.equals(siteUrl) ? "/" : getSiteUri(path);
    }

    private String getSiteUri(String uri) {
        String[] uriElements = uri.split("/");
        String siteUrl = "";
        for (int i = 3; i < uriElements.length; i++) {
            siteUrl = siteUrl.concat("/" + uriElements[i]);
        }
        return siteUrl;
    }

    private List<String> getListOfLemmasFromQuery(String query) {
        List<String> listLemmas = new ArrayList<>();
        try {
            LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
            String[] elementsOfQuery = query.split(" ");
            for (String word : elementsOfQuery) {
                if (!checkWordByServiceForm(word, luceneMorphology)) {
                    String wordBaseForm = luceneMorphology.getNormalForms(word.toLowerCase()).get(0).strip();
                    boolean hasLemmaInRepository = indexingService.getLemmaRepository()
                            .findByLemma(wordBaseForm)
                            .isEmpty();
                    if (!hasLemmaInRepository) {
                        listLemmas.add(wordBaseForm);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return listLemmas;
    }

    private boolean checkWordByServiceForm(String word, LuceneMorphology luceneMorphology) {
        for (String form : SERVICE_FORMS) {
            if (luceneMorphology.getMorphInfo(word.toLowerCase()).get(0).contains(form)) {
                return true;
            }
        }
        return false;
    }

    private void excludingLemmasFromListThereTooMany(List<String> lemmas, Site site) {
        int countPageOnList = indexingService.getPageRepository().findAllBySiteId(site).size();
        int eightyPercentOfSite = (int) (countPageOnList * COEFF_FREQUENCY_LEMMA_ON_PAGES);
        Iterator<String> iterator = lemmas.iterator();

        while (iterator.hasNext()) {
            Lemma lemmaEntity;
            Optional<Lemma> lemmaOptional = indexingService.getLemmaRepository().findByLemma(iterator.next())
                    .stream()
                    .filter(l -> l.getSiteId().getId() == site.getId())
                    .findAny();
            if (lemmaOptional.isPresent()) {
                lemmaEntity = lemmaOptional.get();
                int frequencyOfLemma = lemmaEntity.getFrequency();
                if (frequencyOfLemma >= eightyPercentOfSite) {
                    iterator.remove();
                }
            }
        }
    }

    private List<String> sortLemmasByFrequency(List<String> lemmas) {
        HashMap<String, Integer> mapLemmasByFrequency = new HashMap<>();
        for (String lemma : lemmas) {
            Lemma lemmaFromRepository = indexingService.getLemmaRepository().findByLemma(lemma).get(0);
            int frequency = lemmaFromRepository.getFrequency();
            mapLemmasByFrequency.put(lemma, frequency);
        }
        List<String> sortedLemasList = mapLemmasByFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
        return sortedLemasList;
    }

    private List<String> getFilteredAndSortedListWithLemmas(String query, String site) {
        List<String> lemmas = getListOfLemmasFromQuery(query);
        if (site == null) {
            List<Site> siteList = indexingService.getSitesRepository().findAll();
            for (Site siteEntity : siteList) {
                if (siteEntity.getStatus() == Type.INDEXED || siteEntity.getStatus() == Type.FAILED) {
                    excludingLemmasFromListThereTooMany(lemmas, siteEntity);
                }
            }
        } else {
            Site siteFromRepository = indexingService.getSitesRepository().findByUrl(site).get();
            Iterator<String> iterator = lemmas.iterator();
            while (iterator.hasNext()) {
                Lemma lemma = indexingService.getLemmaRepository().findByLemma(iterator.next()).get(0);
                if (lemma.getSiteId().getId() != siteFromRepository.getId()) {
                    iterator.remove();
                }
            }
            excludingLemmasFromListThereTooMany(lemmas, siteFromRepository);
        }
        return sortLemmasByFrequency(lemmas);
    }
}
