package com.olympus.olympus.msGraphAPI.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuthKeyAttributeDTO {

    private String grantType;
    private String clientId;
    private String clientSecret;
    private String resource;

    public AuthKeyAttributeDTO(String grantType, String resource, String clientId, String clientSecret){
        this.grantType = grantType;
        this.resource = resource;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

}
