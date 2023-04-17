package no.fdk.referencedata.los;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LosRepository extends ListCrudRepository<LosNode, String> {
    List<LosNode> findByUriIn(List<String> uri);
}
