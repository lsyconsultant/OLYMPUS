package com.olympus.olympus.msGraphAPI.service;

import com.olympus.olympus.common.HttpRequest;
import com.olympus.olympus.msGraphAPI.dto.AuthKeyAttributeDTO;
import com.olympus.olympus.msGraphAPI.dto.FileDTO;
import com.olympus.olympus.msGraphAPI.dto.FolderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Map;

@Service
@Slf4j
public class MsGraphApiService {

    @Autowired
    HttpRequest httpRequest;

    @Value("${site.name}")
    private String siteName;
    @Value("${site.tenantId}")
    private String siteTenantId;
    @Value("${site.clientId}")
    private String siteClientId;
    @Value("${site.clientSecret}")
    private String siteClientSecret;
    @Value("${site.grantType}")
    private String siteGrantType;
    @Value("${site.resource}")
    private String siteResource;


    public String getAccessToken() {
        String accessToken = "";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", siteGrantType);
        body.add("client_id", siteClientId);
        body.add("client_secret", siteClientSecret);
        body.add("resource", siteResource);

        String msloginUrl = "https://login.microsoftonline.com/" + siteTenantId + "/oauth2/token";
        Map<String, Object> resonse = httpRequest.postRequest(msloginUrl, body);

        accessToken = resonse.get("access_token").toString();

        return accessToken;
    }

    public String getSiteIdBySiteName(String accessToken) {
        String siteId = "";

        String msloginUrl = "https://graph.microsoft.com/v1.0/sites/lsyconsultant.sharepoint.com:/sites/" + siteName;
        Map<String, Object> resonse = httpRequest.getRequest(msloginUrl, accessToken);

        siteId = resonse.get("id").toString();

        return siteId;
    }

    public ArrayList<FolderDTO> getFilesBcAmtData(String yyyymm, String siteId, String accessToken) {
        String siteUrl = "https://graph.microsoft.com/v1.0/sites/" + siteId;

        //BC별 매출 엑셀 파일(FY22-FY24 May) 폴더 리스트 URL : 01CEJY2NN7ODBWVMUYKFHYD6IY257OKXAQ -> 폴더ID
        String msBcAmtDataDirectoryUrl = siteUrl + "/drive/items/01CEJY2NN7ODBWVMUYKFHYD6IY257OKXAQ/children";
        Map<String, Object> response = httpRequest.getRequest(msBcAmtDataDirectoryUrl, accessToken);

        ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) response.get("value");
        log.info("=> BC별 매출 엑셀 파일(FY22-FY24 May) 폴더들 리스트 : {}", response);

        ArrayList<FolderDTO> folders = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, String> item = list.get(i);

            FolderDTO folder = new FolderDTO();
            folder.setFolderName(item.get("name"));
            folder.setFolderId(item.get("id"));

            String folderUrl = siteUrl + "/drive/items/" + folder.getFolderId() + "/children";
            Map<String, Object> response2 = httpRequest.getRequest(folderUrl, accessToken);

            ArrayList<Map<String, String>> files = (ArrayList<Map<String, String>>) response2.get("value");
            log.info("=> " + folder.getFolderName() + " 폴더 하위에 파일들 : " + files.toString());

            FileDTO file = new FileDTO();
            for (int j = 0; j < files.size(); j++) {
                Map<String, String> temp = files.get(j);

                //file 명명규칙을 yyyymm_orgId_*****.xlsx 로 결정했기 때문에, 언더바(_)로 나눠서 제일 처음이 입력받은 연월과 동일한 것만 가져온다.
                if (temp.get("name").split("_")[0].equals(yyyymm)) {
                    file.setFileName(temp.get("name"));
                    file.setFioleId(temp.get("id"));
                    file.setCreateDatetime(temp.get("createdDateTime"));
                    file.setDownloadUrl(temp.get("@microsoft.graph.downloadUrl"));
                    folder.setFile(file);
                }

            }


            folders.add(folder);
        }

        return folders;
    }


}
