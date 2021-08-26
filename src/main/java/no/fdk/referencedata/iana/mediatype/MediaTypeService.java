package no.fdk.referencedata.iana.mediatype;

import lombok.extern.slf4j.Slf4j;
import no.fdk.referencedata.eu.filetype.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

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
    public void harvestAndSave() {
        try {
            mediaTypeRepository.deleteAll();

            final AtomicInteger counter = new AtomicInteger(0);
            final Iterable<MediaType> iterable = mediaTypeHarvester.harvest().toIterable();
            iterable.forEach(item -> counter.getAndIncrement());
            log.info("Harvest and saving {} media-types", counter.get());
            mediaTypeRepository.saveAll(iterable);

        } catch(Exception e) {
            log.error("Unable to harvest media-types", e);
        }
    }
}
