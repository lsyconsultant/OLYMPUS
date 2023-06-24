package com.olympus.olympus.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olympus.olympus.msGraphAPI.dto.FileDTO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpRequest {

    private RestTemplate restTemplate;

    public  HttpRequest(){
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> postRequest(String url, MultiValueMap<String, String> body){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> requestMessage = new HttpEntity<>(body, httpHeaders);

        Map<String, Object> result = new HashMap<>();
        result.put("access_token", "");
        try{
            HttpEntity<String> response = restTemplate.postForEntity(url, requestMessage, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            result = objectMapper.readValue(response.getBody(), Map.class);
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
    public Map<String, Object> getRequest(String url, String accessToken){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", accessToken);
        httpHeaders.set("Accept", "*/*");

        HttpEntity<?> requestMessage = new HttpEntity<>(httpHeaders);

        Map<String, Object> result = new HashMap<>();
        result.put("id", "");
        try{
            HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestMessage, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            result = objectMapper.readValue(response.getBody(), Map.class);
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }



}
