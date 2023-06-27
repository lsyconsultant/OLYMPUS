package com.olympus.olympus.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olympus.olympus.msGraphAPI.dto.FileDTO;
import org.springframework.batch.item.util.FileUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HttpRequest {

    private RestTemplate restTemplate;

    public HttpRequest() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> postRequest(String url, MultiValueMap<String, String> body) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> requestMessage = new HttpEntity<>(body, httpHeaders);

        Map<String, Object> result = new HashMap<>();
        result.put("access_token", "");
        try {
            HttpEntity<String> response = restTemplate.postForEntity(url, requestMessage, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public Map<String, Object> getRequest(String url, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", "Bearer " + accessToken);
        httpHeaders.set("Accept", "*/*");

        HttpEntity<?> requestMessage = new HttpEntity<>(httpHeaders);

        Map<String, Object> result = new HashMap<>();
        result.put("id", "");
        try {
            HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestMessage, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public Map<String, Object> multiPartRequest(String url, String accessToken, String mergeFilePath) {
        RestTemplate restTemplate = new RestTemplate();

        File file = new File(mergeFilePath);
        long bytes = file.length();
        String range = "bytes 0-" + (bytes - 1) + "/" + bytes;

        Path path = Paths.get(mergeFilePath);
        byte[] fileBytes = null;
        try {
            fileBytes = Files.readAllBytes(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + accessToken);
        httpHeaders.set("Content-Length", String.valueOf(bytes));
        httpHeaders.set("Content-Range", range);


        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileBytes, httpHeaders);

        Map<String, Object> result = new HashMap<>();
        result.put("id", "");
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            result = objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }


    public String getUploadSessionUrl(String temp, String accessToken) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        // 업로드 세션 생성 요청
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

        Map<String, Object> result = new HashMap<>();
        String uploadUrl = "";
        try {
            ResponseEntity<String> response = restTemplate.exchange(temp, HttpMethod.POST, requestEntity, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
            result = objectMapper.readValue(response.getBody(), Map.class);
            uploadUrl = result.get("uploadUrl").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uploadUrl;
    }
}
