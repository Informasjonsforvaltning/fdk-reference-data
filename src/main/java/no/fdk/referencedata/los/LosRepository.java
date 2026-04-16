package no.fdk.referencedata.los;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LosRepository extends JpaRepository<LosNode, String> {
    List<LosNode> findByUriIn(List<String> uri);
}
