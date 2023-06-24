package com.olympus.olympus.msGraphAPI.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

@ToString
@Getter
@Setter
public class FileDTO {
    private String fileName;
    private String fioleId;
    private String createDatetime;
    private String downloadUrl;
}
