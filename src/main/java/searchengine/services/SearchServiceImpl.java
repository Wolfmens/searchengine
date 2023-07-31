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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final IndexingService indexingService;
    private final String[] SERVICE_FORMS = {"ПРЕДЛ", "СОЮЗ", "МЕЖД"};
    private List<String> listLemmas;
    private final float COEFF_FREQUENCY_LEMMA_ON_PAGES = 0.8F;

    @Override
    public Object getSearchResponse(String query, String site, Integer offset, Integer limit) {
        if (query.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of("result", false,
                            "error", "Задан пустой поисковый запрос"));
        }
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = 20;
        }
        listLemmas = getFilteredAndSortedListWithLemmas(query, site);
        Lemma rareLemmaFromRepository = indexingService.getLemmaRepository().findByLemma(listLemmas.get(0)).get(0);
        List<Index> listOfIndexesFromRepositoryByRareLemma = indexingService
                .getIndexLemmaRepository()
                .findAllByLemmaId(rareLemmaFromRepository);
        List<Page> pageList = new ArrayList<>();
        for (Index index : listOfIndexesFromRepositoryByRareLemma) {
            pageList.add(index.getPageId());
        }
        Iterator<Page> iterator = pageList.iterator();

        for (int i = 1; i < listLemmas.size(); i++) {
            Lemma lemma = indexingService.getLemmaRepository().findByLemma(listLemmas.get(i)).get(0);
            List<Index> listOfIndexesFromRepositoryByLemma = indexingService
                                                                            .getIndexLemmaRepository()
                                                                            .findAllByLemmaId(lemma);
            while (iterator.hasNext()) {
                Page page = iterator.next();
                Optional<Index> optional =
                        listOfIndexesFromRepositoryByLemma.stream().filter(in -> in.getPageId().getId() == page.getId())
                                                                   .findAny();
                if (!optional.isPresent()) {
                    iterator.remove();
                }
            }
        }
        //---------------------------------------------------
        HashMap<Page,Integer> mapPageByAbsRelevance = new HashMap<>();
        HashMap<Page,Double> mapPageByOtnRelevance = new HashMap<>();
        for (Page page : pageList){
            int absRel = 0;
            for (String lemma : listLemmas){
                Lemma lemmaFromRepository = indexingService.getLemmaRepository().findByLemma(lemma).get(0);
                Optional<Index> optionalIndex = indexingService.getIndexLemmaRepository()
                        .findByLemmaIdAndPageId(lemmaFromRepository,page);
                if (optionalIndex.isPresent()){
                    absRel += optionalIndex.get().getRank();
                }
            }
            mapPageByAbsRelevance.put(page,absRel);
        }

        int maxAbsRelevance = mapPageByAbsRelevance.entrySet().stream()
                                                                        .max(Map.Entry.comparingByValue())
                                                                        .get()
                                                                        .getValue();
        for (Map.Entry<Page,Integer> entry : mapPageByAbsRelevance.entrySet()){
            double otnRel = (double) entry.getValue() / maxAbsRelevance;
            mapPageByOtnRelevance.put(entry.getKey(),otnRel);
        }




        SearchResponse searchResponse = new SearchResponse();

        searchResponse.setResult(true);
        List<DataSearchItem> listData = getListObjectsBySearch(mapPageByOtnRelevance);
        searchResponse.setData(listData);


        return searchResponse;
    }

    private Map<Page,Double> getSortedMapPagesByOtnRel (Map<Page,Double> map){
        map.entrySet().stream().sorted(Map.Entry.<Page,Double>comparingByValue().reversed());
        return map;
    }

    private List<DataSearchItem> getListObjectsBySearch (Map<Page,Double> map){
        Map<Page,Double> sortedMap = map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(LinkedHashMap::new,(m,c) -> m.put(c.getKey(),c.getValue()),LinkedHashMap::putAll);
        List<DataSearchItem> list = new ArrayList<>();

        for (Map.Entry<Page,Double> entry : sortedMap.entrySet()){
            Page page = entry.getKey();
            Site site = page.getSiteId();
            double otnRel = entry.getValue();
            DataSearchItem dataSearchItem = new DataSearchItem();
            dataSearchItem.setRelevance(otnRel);
            dataSearchItem.setSiteName(site.getName());
            dataSearchItem.setSite(site.getUrl());
            if (page.getPath().equals(site.getUrl())){
                dataSearchItem.setUri("/");
            } else {
                String uri = getSiteUri(page.getPath());
                dataSearchItem.setUri(uri);
            }
            try {
                Document document = Jsoup.connect(page.getPath()).get();
                String title = document.title();
                dataSearchItem.setTitle(title);
                // найти в тексте совпадения, поиск по всем сайтам
            } catch (Exception e){
                e.printStackTrace();
            }




            list.add(dataSearchItem);
        }
        return list;
    }

    private String getSiteUri (String uri) {
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
                    listLemmas.add(wordBaseForm);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return listLemmas;
    }

    private boolean checkWordByServiceForm(String word, LuceneMorphology luceneMorphology) {
        for (String form : SERVICE_FORMS) {
            if (luceneMorphology.getMorphInfo(word).get(0).contains(form)) {
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
            } else {
                iterator.remove();
            }
            // проверить есть ли такая лемма, если есть то продолжать
//            int frequencyOfLemma = lemmaEntity.getFrequency();
//            if (frequencyOfLemma >= eightyPercentOfSite){
//                iterator.remove();
//            }
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
        // с начало проверить есть ли такие леммы в базе, если есть, то продолжать если нет, то пустой список
        if (site == null) {
            List<Site> siteList = indexingService.getSitesRepository().findAll();
            for (Site siteEntity : siteList) {
                if (siteEntity.getStatus() == Type.INDEXED || siteEntity.getStatus() == Type.FAILED) {
                    excludingLemmasFromListThereTooMany(lemmas, siteEntity);
                }
            }
        } else {
            Site siteFromRepository = indexingService.getSitesRepository().findByUrl(site).get();
            excludingLemmasFromListThereTooMany(lemmas, siteFromRepository);
        }
        return sortLemmasByFrequency(lemmas);
    }
}
