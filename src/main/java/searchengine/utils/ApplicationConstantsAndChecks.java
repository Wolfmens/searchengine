package searchengine.utils;

import org.apache.lucene.morphology.LuceneMorphology;

import java.text.DecimalFormat;

public interface ApplicationConstantsAndChecks {

    String PRETEXT_FORMS = "ПРЕДЛ";
    String UNION_FORMS = "СОЮЗ";
    String INTERJECTION_FORMS = "МЕЖД";

    float COEFF_FREQUENCY_LEMMA_ON_PAGES = 0.8F;
    int SIZE_LIMIT_OF_SNIPPET = 180;

    String PNG_FORMAT = ".png";
    String JPEG_FORMAT = ".jpeg";
    String JPG_FORMAT = ".jpg";
    String PDF_FORMAT = ".pdf";

    DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#####");
    String ENGLISH_CHECK_REGEX = "[A-Za-z]*";
    String EXCEPT_RUS_LETTERS_REGEX = "^а-яА-Я";
    String EXCEPT_US_LETTERS_REGEX = "^a-zA-Z";
    String REGEX_CHECK_QUERY_FOR_NOT_WORD = "[A-Za-zА-Яа-я\\s]*";

    static boolean checkWordByServiceForm(String word, LuceneMorphology luceneMorphology) {
        String normalFormOfWord = luceneMorphology.getMorphInfo(word.toLowerCase()).get(0);

        return normalFormOfWord.contains(ApplicationConstantsAndChecks.PRETEXT_FORMS) ||
                normalFormOfWord.contains(ApplicationConstantsAndChecks.UNION_FORMS) ||
                normalFormOfWord.contains(ApplicationConstantsAndChecks.INTERJECTION_FORMS);
    }

    static boolean hasUrlIsNotImage(String url) {
        return url.contains(PDF_FORMAT) ||
                url.contains(JPEG_FORMAT) ||
                url.contains(JPG_FORMAT) ||
                url.contains(PNG_FORMAT);


    }
}
