package no.fdk.referencedata.los;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LosService {

    private List<LosNode> allLosNodes = Collections.emptyList();

    public LosImporter losImporter;

    @Autowired
    public LosService(LosImporter losImporter) {
        this.losImporter = losImporter;
    }

    public List<LosNode> getByURIs(List<String> uris) {
        return allLosNodes.stream()
                .filter(losNode -> uris.contains(losNode.getUri()))
                .sorted(Comparator.comparing(LosNode::getUri))
                .collect(Collectors.toList());
    }

    public List<LosNode> getAll() {
        return allLosNodes;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importLosNodes() {
        log.debug("Importing los nodes");
        allLosNodes = losImporter.importFromLosSource();
    }
}
