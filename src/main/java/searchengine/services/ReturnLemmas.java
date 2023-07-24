package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;


import java.util.*;

@RequiredArgsConstructor
public class ReturnLemmas {

    private final String[] SERVICE_FORMS = {"ПРЕДЛ", "СОЮЗ", "МЕЖД"};
    private final IndexingService indexingService;
    private final Site site;
    private final Page page;
    private final HashMap<String, Integer> lemmasMap = new HashMap<>();


    public void getLemmas(String text) throws Exception {
        String textNew = getStringsOfURL(text);
        String regex = "[^а-яА-Я\\s]";
        String newtext = textNew.toLowerCase().replaceAll(regex, " ").strip();
//        if (textNew.equals("")) {
//            textNew = "";
//        }
        String[] textForGetLemmas = newtext.split("\\s+");
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        List<String> wordBaseForms = new ArrayList<>();
        HashSet<String> a = new HashSet<>();
        for (String word : textForGetLemmas) {
            if (checkWordByServiceForm(word, luceneMorphology)) {
                continue;
            } else {
                String wordBaseForm = luceneMorphology.getNormalForms(word.toLowerCase()).get(0).strip();
                if (!wordBaseForms.contains(wordBaseForm)) {
                    addLemmaToRepository(wordBaseForm);
                }
                wordBaseForms.add(wordBaseForm);
                addLemmaToMap(wordBaseForm);
            }
        }
//        wordBaseForms.forEach(this::addLemmaToRepository);
//        a.forEach(this::addLemmaToRepository);
//        wordBaseForms.forEach(this::addLemmaToMap);
        addIndexToRepository();
    }

    private void addIndexToRepository() {
        for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
            Index index = new Index();
            Lemma lemma = indexingService.getLemmaRepository().findByLemma(entry.getKey()).get();
            index.setPageId(page);
            index.setLemmaId(lemma);
            index.setRank(entry.getValue());
            indexingService.getIndexLemmaRepository().save(index);
        }
    }

    private void addLemmaToMap(String lemma) {
        if (lemmasMap.containsKey(lemma)) {
            lemmasMap.put(lemma, lemmasMap.get(lemma) + 1);
        } else {
            lemmasMap.put(lemma, 1);
        }
    }

    private boolean checkWordByServiceForm(String word, LuceneMorphology luceneMorphology) {
        for (String form : SERVICE_FORMS) {
            if (luceneMorphology.getMorphInfo(word).get(0).contains(form)) {
                return true;
            }
        }
        return false;
    }

    private String getStringsOfURL(String uri) throws Exception {
        return uri.replaceAll("[^А-Яа-я]+", " ");
    }

    private boolean hasLemmaInRepository(String lemma) {
        Optional<Lemma> hasLemma = indexingService.getLemmaRepository().findByLemma(lemma);
        return hasLemma.isPresent();
    }

    private void addLemmaToRepository(String lemma) {
        Lemma lemmaEntity;
        if (hasLemmaInRepository(lemma)) {
            lemmaEntity = indexingService.getLemmaRepository().findByLemma(lemma).get();
            int frequency = lemmaEntity.getFrequency();
            lemmaEntity.setFrequency(frequency + 1);
        } else {
            lemmaEntity = new Lemma();
            lemmaEntity.setFrequency(1);
            lemmaEntity.setSiteId(site);
            lemmaEntity.setLemma(lemma);
        }
        indexingService.getLemmaRepository().save(lemmaEntity);
    }

    public void updateLemmas(String content) throws Exception {
        String textNew = getStringsOfURL(content);
        String regex = "[^а-яА-Я\\s]";
        String newtext = textNew.toLowerCase().replaceAll(regex, " ").strip();
        String[] textForGetLemmas = newtext.split("\\s+");
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        for (String word : textForGetLemmas) {
            if (checkWordByServiceForm(word, luceneMorphology)) {
                continue;
            } else {
                String wordBaseForm = luceneMorphology.getNormalForms(word.toLowerCase()).get(0).strip();
                addLemmaToMap(wordBaseForm);
            }
        }
        List<Index> indexesByPage = indexingService.getIndexLemmaRepository().findAllByPageId(page);
        for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
            Optional<Lemma> lemma = indexingService.getLemmaRepository().findByLemma(entry.getKey());
            if (lemma.isPresent()) {
                Optional<Index> index = indexesByPage.stream()
                        .filter(i -> i.getLemmaId().getId() == lemma.get().getId())
                        .findFirst();
                if (index.isPresent()) {
                    if (index.get().getRank() != entry.getValue()) {
                        index.get().setRank(entry.getValue());
                        indexingService.getIndexLemmaRepository().save(index.get());
                    }
                } else {
                    Index newIndex = new Index();
                    newIndex.setPageId(page);
                    newIndex.setLemmaId(lemma.get());
                    newIndex.setRank(entry.getValue());
                    indexingService.getIndexLemmaRepository().save(newIndex);
                }
            } else {
                addLemmaToRepository(entry.getKey());
                Lemma lemmaFromRepository = indexingService.getLemmaRepository().findByLemma(entry.getKey()).get();
                Index newIndex = new Index();
                newIndex.setPageId(page);
                newIndex.setLemmaId(lemmaFromRepository);
                newIndex.setRank(entry.getValue());
                indexingService.getIndexLemmaRepository().save(newIndex);
            }
        }
    }
}

//    public HashMap<String, Integer> getLemmas(String text) throws Exception {
//        HashMap<String, Integer> lemmasMap = new HashMap<>();
//        String textNew = getStringsOfURL(text);
//        String regex = "[^а-яА-Я\\s]";
//        String newtext = textNew.toLowerCase().replaceAll(regex, " ").strip();
////        if (textNew.equals("")) {
////            textNew = "";
////        }
//        String[] textForGetLemmas = newtext.split("\\s+");
//        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
//        List<String> wordBaseForms = new ArrayList<>();
//        for (String word : textForGetLemmas) {
//            if (checkWordByServiceForm(word, luceneMorphology)) {
//                continue;
//            } else {
//                wordBaseForms.add(luceneMorphology.getNormalForms(word.toLowerCase()).get(0));
//            }
//        }
////        wordBaseForms.forEach(s -> {
////            if (lemmasMap.containsKey(s)) {
////                lemmasMap.put(s, lemmasMap.get(s) + 1);
////            } else {
////                lemmasMap.put(s, 1);
////            }
////        });
//        return lemmasMap;
//    }
