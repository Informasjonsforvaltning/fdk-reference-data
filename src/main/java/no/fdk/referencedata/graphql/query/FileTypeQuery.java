package no.fdk.referencedata.graphql.query;

import no.fdk.referencedata.eu.filetype.FileType;
import no.fdk.referencedata.eu.filetype.FileTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class FileTypeQuery {

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @QueryMapping
    public List<FileType> fileTypes() {
        return StreamSupport.stream(fileTypeRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparing(FileType::getUri))
                .collect(Collectors.toList());
    }

    @QueryMapping
    public FileType fileTypeByCode(@Argument String code) {
        return fileTypeRepository.findByCode(code).orElse(null);
    }
}
