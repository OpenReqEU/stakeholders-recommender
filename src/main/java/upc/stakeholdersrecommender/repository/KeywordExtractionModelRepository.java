package upc.stakeholdersrecommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import upc.stakeholdersrecommender.entity.Effort;
import upc.stakeholdersrecommender.entity.KeywordExtractionModel;

@Repository
public interface KeywordExtractionModelRepository extends JpaRepository<KeywordExtractionModel, String> {

}

