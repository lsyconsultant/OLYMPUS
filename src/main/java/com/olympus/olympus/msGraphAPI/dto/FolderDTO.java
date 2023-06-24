package com.olympus.olympus.msGraphAPI.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

@ToString
@Getter
@Setter
public class FolderDTO {
    private String folderName;
    private String folderId;
    private FileDTO file;

}
