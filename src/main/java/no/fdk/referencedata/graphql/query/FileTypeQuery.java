package no.fdk.referencedata.graphql.query;

import graphql.kickstart.tools.GraphQLQueryResolver;
import no.fdk.referencedata.eu.filetype.FileType;
import no.fdk.referencedata.eu.filetype.FileTypeRepository;
import no.fdk.referencedata.iana.mediatype.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class FileTypeQuery implements GraphQLQueryResolver {

    @Autowired
    private FileTypeRepository fileTypeRepository;

    public List<FileType> getFileTypes() {
        return StreamSupport.stream(fileTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(FileType::getUri))
                .collect(Collectors.toList());
    }

    public FileType getFileTypeByCode(String code) {
        return fileTypeRepository.findByCode(code).orElse(null);
    }
}