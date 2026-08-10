package no.fdk.referencedata.graphql.query;

import lombok.RequiredArgsConstructor;
import no.fdk.referencedata.core.CodeListQuerySupport;
import no.fdk.referencedata.eu.filetype.FileType;
import no.fdk.referencedata.eu.filetype.FileTypeRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class FileTypeQuery {

    private final FileTypeRepository fileTypeRepository;
    private final CodeListQuerySupport support;

    @QueryMapping
    public List<FileType> fileTypes() {
        return support.allSortedByUri(fileTypeRepository, FileType::getUri);
    }

    @QueryMapping
    public FileType fileTypeByCode(@Argument String code) {
        return support.byCode(fileTypeRepository::findByCode, code);
    }
}
