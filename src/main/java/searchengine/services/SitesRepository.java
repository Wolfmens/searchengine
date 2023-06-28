package searchengine.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;

@Repository
public interface SitesRepository extends JpaRepository<Site,Integer> {

}
