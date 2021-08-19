package no.fdk.referencedata.iana.mediatype;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MediaTypeService {

    private final MediaTypeHarvester mediaTypeHarvester;

    private final MediaTypeRepository mediaTypeRepository;

    @Autowired
    public MediaTypeService(MediaTypeHarvester mediaTypeHarvester,
                            MediaTypeRepository mediaTypeRepository) {
        this.mediaTypeHarvester = mediaTypeHarvester;
        this.mediaTypeRepository = mediaTypeRepository;
    }

    @Transactional
    public void harvestAndSaveMediaTypes() {
        try {
            mediaTypeRepository.deleteAll();
            mediaTypeRepository.saveAll(mediaTypeHarvester.harvest().toIterable());
        } catch(Exception e) {
            log.error("Unable to harvest media-types", e);
        }
    }
}
