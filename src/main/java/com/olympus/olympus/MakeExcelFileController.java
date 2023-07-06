package com.olympus.olympus;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class MakeExcelFileController {

    @Autowired
    MakeExcelFileWebService makeExcelFileWebService;

    @Value("${projectFolder}")
    private String projectFolder;


    @GetMapping("/sessionSetWorkingFilePathMerge.do")
    public @ResponseBody String sessionSetWorkingFilePathMerge(HttpServletRequest req) {
        String workingFilePath = "";

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        String folderName = new SimpleDateFormat("yyyyMMdd-HHmmss").format(currentTimestamp);

        workingFilePath = projectFolder + "\\" + folderName;
        File Folder = new File(workingFilePath);
        if (!Folder.exists()) {
            Folder.mkdir();
        }

        HttpSession session = req.getSession();
        session.setAttribute("workingFilePath", workingFilePath);

        return workingFilePath;
    }

    @PostMapping("/bcAmtMerge.do")
    public @ResponseBody String  selectJobList(@RequestBody Map<String, String> dto) {
        log.info("/bcAmtMerge.do 호출시 넘어온 파라미터 : {}", dto.toString());
        ResponseDTO result = new ResponseDTO();
        String mergeFilePath = makeExcelFileWebService.execute(dto);
        System.gc();
        return mergeFilePath;
    }

    @PostMapping("/upload.do")
    public @ResponseBody ResponseEntity<String> selectJobList(@RequestPart("file") MultipartFile file, HttpServletRequest req) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("업로드할 파일을 선택해주세요.");
        }

        // 업로드할 파일명을 생성합니다.
        String fileName = file.getOriginalFilename();
        if(fileName.equals("masterFile.csv")){
            fileName = "before_" + fileName;
        }
        String filePath = req.getSession().getAttribute("workingFilePath").toString() + File.separator + fileName;

        try {
            // 파일을 실제 경로로 저장합니다.
            file.transferTo(new File(filePath));
            return ResponseEntity.ok(fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 중 오류가 발생했습니다.");
        }
    }


}
