package searchengine.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;
import java.util.Optional;

public interface IndexLemmaRepository extends JpaRepository<Index,Integer> {

    @Transactional
    List<Index> deleteByPageId(Page s);

    Optional<Index> findByPageId(Page page);

    List<Index> findAllByPageId(Page page);

    List<Index> findAllByLemmaId(Lemma lemma);

    Optional<Index> findByLemmaIdAndPageId (Lemma lemma, Page page);
}
