package no.fdk.referencedata.filetype;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FileTypes {
    List<FileType> fileTypes;
}
