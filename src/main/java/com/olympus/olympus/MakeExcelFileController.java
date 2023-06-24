package com.olympus.olympus;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

@Slf4j
@Controller
public class MakeExcelFileController {

    @Autowired
    MakeExcelFileService makeExcelFileService;

    @GetMapping("/makeFile.do")
    public @ResponseBody ResponseDTO selectJobList(HttpServletRequest req){
        ResponseDTO result = new ResponseDTO();

        return result;
    }


}
