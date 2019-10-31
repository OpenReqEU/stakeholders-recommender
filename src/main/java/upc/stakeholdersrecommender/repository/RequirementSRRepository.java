package upc.stakeholdersrecommender.repository;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import upc.stakeholdersrecommender.entity.RequirementSR;
import upc.stakeholdersrecommender.entity.RequirementSRId;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface RequirementSRRepository extends JpaRepository<RequirementSR, String> {
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Fetch(value = FetchMode.SELECT)
    RequirementSR findById(RequirementSRId id);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Transactional
    void deleteByOrganization(String organization);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Fetch(value = FetchMode.SELECT)
    List<RequirementSR> findByOrganization(String organization);

    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "50000")})
    @Fetch(value = FetchMode.SELECT)
    List<RequirementSR> findByOrganizationAndProj(String organization, String id);

}
