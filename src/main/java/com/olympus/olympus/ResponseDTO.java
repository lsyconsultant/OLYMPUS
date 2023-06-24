package com.olympus.olympus;

import java.util.HashMap;
import java.util.Map;

public class ResponseDTO {

    private String resultCd;
    private String resultMsg;
    private Map<String, Object> data;

    public ResponseDTO() {
        this.data = new HashMap<>();
    }

    @Override
    public String toString() {
        return "ResponseDTO{" +
                "resultCd='" + resultCd + '\'' +
                ", resultMsg='" + resultMsg + '\'' +
                ", data=" + data +
                '}';
    }

    public void setResult(String resultCd, String resuiltMsg, Map<String, Object> data) {
        this.resultCd = resultCd;
        this.resultMsg = resuiltMsg;
        this.data = data;
    }

    public String getResultCd() {
        return resultCd;
    }

    public void setResultCd(String resultCd) {
        this.resultCd = resultCd;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
