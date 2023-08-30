package searchengine.services;

import lombok.AllArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.search.DataSearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.model.*;
import searchengine.utils.ApplicationConstantsAndChecks;

import java.io.IOException;
import java.util.*;

@Service
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final IndexingService indexingService;
    private List<String> listLemmas;
    private List<Page> pageList = new ArrayList<>();
    private LuceneMorphology luceneMorphology;
    private HashMap<String,Integer> mapOfCountPagesBySite;

    @Override
    public Object getSearchResponse(String query, String site, Integer offset, Integer limit) {
        if (query.isEmpty() || !query.matches("[A-Za-zА-Яа-я\\s]*")) {
            return getGeneratedResponse(true, new ArrayList<>(), 0);
        }
        offset = getValueOffset(offset);
        limit = getValueLimit(limit);

        mapOfCountPagesBySite = indexingService.getCountPagesBySite();
        listLemmas = getFilteredAndSortedListWithLemmas(query, site);

        if (listLemmas.isEmpty()) {
            if (isEnglishQuery(query)) {
                luceneMorphology = getRussianLuceneMorphology();
            }
            return getGeneratedResponse(true, new ArrayList<>(), 0);
        }
        fillPageList(site);
        if (pageList.isEmpty()){
            return getGeneratedResponse(true, new ArrayList<>(), 0);
        }
        HashMap<Page, Double> mapPageByOtnRelevance = getMapPageByOtnRelevance(pageList);
        HashMap<Page, Double> sortedMapPageByOtnRelevance = getSortedMapPageByOtnRelevance(mapPageByOtnRelevance);
        List<DataSearchItem> listData = getListObjectsBySearch(sortedMapPageByOtnRelevance, listLemmas, query);
        List<DataSearchItem> listDataByLimitAndOffset = listData.stream().skip(offset).limit(limit).toList();

        luceneMorphology = getRussianLuceneMorphology();
        pageList.clear();

        return getGeneratedResponse(true, listDataByLimitAndOffset, listData.size());
    }



    private boolean isEnglishQuery(String query) {
        String[] queryElements = query.split(" ");
        Optional<String> isOptionalCheckLanguage = Arrays.stream(queryElements)
                        .filter(el -> el.matches(ApplicationConstantsAndChecks.ENGLISH_CHECK_REGEX)).findFirst();
        return isOptionalCheckLanguage.isPresent();
    }

    private LuceneMorphology getEnglishLuceneMorphology () {
        try {
            return new EnglishLuceneMorphology();
        } catch (IOException ex) {
            System.err.println(ex.getClass() + "---" + ex.getMessage());;
        }
        return null;
    }

    private LuceneMorphology getRussianLuceneMorphology () {
        try {
            return new RussianLuceneMorphology();
        } catch (IOException ex) {
            System.err.println(ex.getClass() + "---" + ex.getMessage());;
        }
        return null;
    }


    private int getValueLimit(Integer limit) {
        if (limit == null) {
            return 20;
        }
        return limit;
    }

    private int getValueOffset(Integer offset) {
        if (offset == null) {
           return 0;
        }
        return offset;
    }

    private void fillPageList (String site){
        if (site == null) {
            for (Site siteFromRepository : indexingService.getSitesRepository().findAll()) {
                List<String> listLemmasFilter = new ArrayList<>();
                for (String wordLemma : listLemmas){
                    Optional<Lemma> optionalLemma = indexingService.getLemmaRepository()
                            .findByLemma(wordLemma)
                            .stream()
                            .filter(l -> l.getSiteId().getId() == siteFromRepository.getId())
                            .findFirst();
                    if (optionalLemma.isPresent()){
                        listLemmasFilter.add(wordLemma);
                    }
                }
                if (!listLemmasFilter.isEmpty()) {
                    pageList.addAll(getFilterPageList(siteFromRepository.getUrl(), listLemmasFilter));
                }
            }
        } else {
            pageList.addAll(getFilterPageList(site, listLemmas));
        }
    }

    private SearchResponse getGeneratedResponse(boolean result, List<DataSearchItem> list, int count) {
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setResult(result);
        searchResponse.setData(list);
        searchResponse.setCount(count);

        return searchResponse;
    }

    private HashMap<Page, Double> getMapPageByOtnRelevance(List<Page> pageList) {
        HashMap<Page, Float> mapPageByAbsRelevance = new HashMap<>();
        HashMap<Page, Double> mapPageByOtnRelevance = new HashMap<>();
        List<Lemma> list = new ArrayList<>();
        listLemmas.forEach(l -> list.addAll(indexingService.getLemmaRepository().findByLemma(l)));

        for (Page page : pageList) {
            float absRel = 0;
            for (Lemma lemma : list) {
                Optional<Index> optionalIndex = indexingService.getIndexLemmaRepository()
                        .findByLemmaIdAndPageId(lemma, page);
                if (optionalIndex.isPresent()) {
                    absRel += optionalIndex.get().getRank();
                }
            }
            mapPageByAbsRelevance.put(page, absRel);
        }
        float maxAbsRelevance = mapPageByAbsRelevance.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getValue();
        for (Map.Entry<Page, Float> entry : mapPageByAbsRelevance.entrySet()) {
            double otnRel = (double) entry.getValue() / maxAbsRelevance;
            mapPageByOtnRelevance.put(entry.getKey(), otnRel);
        }
        return mapPageByOtnRelevance;
    }

    private HashMap<Page,Double> getSortedMapPageByOtnRelevance (HashMap<Page,Double> map){
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(LinkedHashMap::new, (m, c) -> m.put(c.getKey(), c.getValue()), LinkedHashMap::putAll);
    }

    private List<Page> getFilterPageList(String site, List<String> listLemmas) {
        Site siteFromRepository = indexingService.getSitesRepository().findByUrl(site).get();
        List<Page> pageListByLemmas = getPageListByRarestLemma(listLemmas,siteFromRepository);
        if (pageListByLemmas.isEmpty()) {
            return pageListByLemmas;
        }
        Iterator<Page> iterator = pageListByLemmas.iterator();
        for (int i = 1; i < listLemmas.size(); i++) {
            Optional<Lemma> lemmaOptionalByNextLemma = getOptionalByLemma(listLemmas.get(i),siteFromRepository);
            if (lemmaOptionalByNextLemma.isPresent()) {
                Lemma lemma = lemmaOptionalByNextLemma.get();
                List<Index> listOfIndexesFromRepositoryByLemma =
                        indexingService.getIndexLemmaRepository().findAllByLemmaId(lemma);
                while (iterator.hasNext()) {
                    Page page = iterator.next();
                    Optional<Index> optional = listOfIndexesFromRepositoryByLemma.stream()
                            .filter(in -> in.getPageId().getId() == page.getId())
                            .findAny();
                    if (!optional.isPresent() && !optional.isEmpty()) {
                        iterator.remove();
                    }
                }
            }
        }
        return pageListByLemmas;
    }

    private List<Page> getPageListByRarestLemma (List<String> listLemmas, Site site) {
        Optional<Lemma> rarestLemmaOptional = getOptionalByLemma(listLemmas.get(0),site);
        Lemma rarestLemmaFromRepository = rarestLemmaOptional.orElse(new Lemma());
        return indexingService.getIndexLemmaRepository()
                .findAllByLemmaId(rarestLemmaFromRepository).stream()
                .filter(i -> i.getPageId().getSiteId().getId() == site.getId())
                .map(Index::getPageId)
                .toList();
    }

    private Optional<Lemma> getOptionalByLemma(String lemma, Site site) {
        return indexingService.getLemmaRepository()
                .findByLemma(lemma)
                .stream()
                .filter(l -> l.getSiteId().getId() == site.getId())
                .findFirst();
    }

    private List<DataSearchItem> getListObjectsBySearch(Map<Page, Double> map, List<String> lemmas, String query) {
        List<DataSearchItem> list = new ArrayList<>();

        for (Map.Entry<Page, Double> entry : map.entrySet()) {
            HashSet<String> setSentencesFromPage = new HashSet<>();
            HashSet<String> setSentencesFromPageWithSecretions = new HashSet<>();
            Page page = entry.getKey();
            Site site = page.getSiteId();
            StringBuilder builder = new StringBuilder();

            fillingSetSentences(query,lemmas,page,setSentencesFromPage);
            fillingSetSentencesWithSecretions(setSentencesFromPage,query,setSentencesFromPageWithSecretions);
            fillingBuilderSnippet(setSentencesFromPageWithSecretions,builder);

            if (builder.toString().isBlank()) {
                continue;
            }
            double otnRel = getOntRelValue(entry.getValue());
            String uri = getUri(page.getPath(), site.getUrl());
            String htmlContent = indexingService.getPageRepository().findById(page.getId()).get().getContent();
            Document document = Jsoup.parse(htmlContent);
            list.add(new DataSearchItem(site.getUrl(),
                    site.getName(),
                    uri,
                    document.title(),
                    builder.toString().strip(),
                    otnRel));
        }
        return list;
    }

    private double getOntRelValue(double oldValue) {
        String replaceSign =
                ApplicationConstantsAndChecks.DECIMAL_FORMAT.format(oldValue).replace(",", ".");
        return Double.parseDouble(replaceSign);
    }

    private void fillingSetSentences(String query,
                                     List<String> lemmas,
                                     Page page,
                                     HashSet<String> set) {
        String[] queryElements = query.split(" ");
        for (String queryWord : queryElements) {
            if (isEnglishQuery(query)){
                luceneMorphology = queryWord.matches(ApplicationConstantsAndChecks.ENGLISH_CHECK_REGEX) ?
                        getEnglishLuceneMorphology():
                        getRussianLuceneMorphology();
            }
            String lemmaFromQuery = luceneMorphology.getNormalForms(queryWord.toLowerCase()).get(0).strip();
            if (!lemmas.contains(lemmaFromQuery)) {
                continue;
            }
            HashMap<String, Set<String>> mapWordsAndSentencesWithThem
                    = indexingService.getMapOfPositionWordsByPage().get(page.getId());
            Set<String> setSentencesByPageFromBase = mapWordsAndSentencesWithThem.get(queryWord);
            if (setSentencesByPageFromBase == null) {
                continue;
            }
            set.addAll(setSentencesByPageFromBase);
        }
    }

    private void fillingSetSentencesWithSecretions(HashSet<String> setBeforeChange,
                                                   String query,
                                                   HashSet<String> setAfterChange) {
        String[] queryElements = query.split(" ");
        for (String sentence : setBeforeChange) {
            String wordChanged = sentence;
            for (String wordFromQuery : queryElements) {
                if (!sentence.contains(wordFromQuery)) {
                    continue;
                }
                wordChanged = wordChanged.replace(wordFromQuery, "<b>" + wordFromQuery + "</b>");
            }
            setAfterChange.add(wordChanged);
        }
    }

    private void fillingBuilderSnippet(HashSet<String> set, StringBuilder builder) {
        for (String sentence : set){
            if (builder.length() <= ApplicationConstantsAndChecks.SIZE_LIMIT_OF_SNIPPET){
                builder.append(sentence).append("...");
            }
        }
    }

    private String getUri(String path, String siteUrl) {
        return path.equals(siteUrl) ? "/" : getSiteUri(path, siteUrl);
    }

    private String getSiteUri(String path, String site) {
        return path.replace(site,"");
    }

    private List<String> getListOfLemmasFromQuery(String query) {
        List<String> listLemmas = new ArrayList<>();
        try {
            String[] elementsOfQuery = query.split(" ");
            for (String word : elementsOfQuery) {
                if (isEnglishQuery(query)){
                    luceneMorphology = word.matches(ApplicationConstantsAndChecks.ENGLISH_CHECK_REGEX) ?
                            getEnglishLuceneMorphology():
                            getRussianLuceneMorphology();
                }
                if (!ApplicationConstantsAndChecks.checkWordByServiceForm(word, luceneMorphology)) {
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
            System.err.println(ex.getClass() + "---" + ex.getMessage());
        }
        return listLemmas;
    }

    private void excludingLemmasFromListThereTooMany(List<String> lemmas, Site site) {
        int countPageOnList = mapOfCountPagesBySite.get(site.getUrl());
        int eightyPercentOfSite =
                (int) (countPageOnList * ApplicationConstantsAndChecks.COEFF_FREQUENCY_LEMMA_ON_PAGES);
        Iterator<String> iterator = lemmas.iterator();

        while (iterator.hasNext()) {
            Lemma lemmaEntity;
            Optional<Lemma> lemmaOptional = indexingService.getLemmaRepository()
                    .findByLemma(iterator.next())
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
        return mapLemmasByFrequency.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
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
                Optional<Lemma> lemmaOption =
                        indexingService.getLemmaRepository()
                                .findByLemma(iterator.next())
                                .stream()
                                .filter(l -> l.getSiteId().getId() == siteFromRepository.getId())
                                .findFirst();
                if (!lemmaOption.isPresent()) {
                    iterator.remove();
                }
            }
            excludingLemmasFromListThereTooMany(lemmas, siteFromRepository);
        }
        return sortLemmasByFrequency(lemmas);
    }
}
