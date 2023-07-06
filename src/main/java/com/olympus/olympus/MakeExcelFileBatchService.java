package com.olympus.olympus;

import com.olympus.olympus.msGraphAPI.dto.FileDTO;
import com.olympus.olympus.msGraphAPI.dto.FolderDTO;
import com.olympus.olympus.msGraphAPI.service.MsGraphApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Slf4j
public class MakeExcelFileBatchService {

    @Autowired
    MsGraphApiService ms;

    @Value("${projectFolder}")
    private String projectFolder;

    //    @Scheduled(fixedDelay = 6000000) // 10초마다 실행
    public void scheduleExecute() {
        execute("202305");
    }

    public void execute(String yyyymm) {

        log.info("★★★★★★★★프로그램 시작★★★★★★★★");

        log.info("==> 1단계 : [MS Graph API] 사용에 필요한 Access Token 가져오기");
        String accessToken = ms.getAccessToken();
        log.info("Access Token : {}", accessToken);
        if (accessToken.equals("")) {
            log.info("Access Token 가져오기 실패함!!!!!!!");
            return;
        }


        log.info("==> 2단계 : [MS Graph API] site.nam 속성을 이용하여, sharepoint id 가져오기");
        String siteId = ms.getSiteIdBySiteName(accessToken);
        log.info("Site Id : {}", siteId);
        if (accessToken.equals("")) {
            log.info("Site Id 가져오기 실패함!!!!!!!");
            return;
        }


        log.info("==> 3단계 : [MS Graph API] sharepoint id를 이용하여, BC별 매출 엑셀 파일 찾기");
        //지사별 엑셀파일이 없을 경우 에러는 나지 않지만, 매출 데이터 없다는 문자 메시지는 보내주는 것도 나쁘지 않을듯
        ArrayList<FolderDTO> folderSubFiles = ms.getFilesBcAmtData(yyyymm, siteId, accessToken);
        log.info("=> BC별 매출 엑셀 파일(FY22-FY24 May) 내의 지사별 매출 데이터 파일 : {}", folderSubFiles);


        log.info("==> 4단계 : [MS Graph API] 매출데이터 파일을 로컬로 다운로드");
        Map<String, String> localExcelFiles = fileUrlDownloadToLocalPath(folderSubFiles);
//        Map<String, String> localExcelFiles = new HashMap<>();
//        localExcelFiles.put("folderPath", "c:\\olympus\\20230626-160910");
//        localExcelFiles.put("baseFilePath", "c:\\olympus\\20230626-160910\\202305_OKR_GIET_sales.xlsx");
//        localExcelFiles.put("202305_OVN_GIET_sales.xlsx", "c:\\olympus\\20230626-160910\\202305_OVN_GIET_sales.xlsx");
//        localExcelFiles.put("202305_OAZ_GIET_sales.xlsx", "c:\\olympus\\20230626-160910\\202305_OAZ_GIET_sales.xlsx");
//        localExcelFiles.put("202305_OTH_GIET_sales.xlsx", "c:\\olympus\\20230626-160910\\202305_OTH_GIET_sales.xlsx");
//        localExcelFiles.put("202305_OHC_GIET_sales.xlsx", "c:\\olympus\\20230626-160910\\202305_OHC_GIET_sales.xlsx");
//        localExcelFiles.put("202305_OSP_GIET_sales.xlsx", "c:\\olympus\\20230626-160910\\202305_OSP_GIET_sales.xlsx");
//        localExcelFiles.put("202305_OMSI_GIET_sales.xlsx", "c:\\olympus\\20230626-160910\\202305_OMSI_GIET_sales.xlsx");
//        localExcelFiles.put("202305_OML_GIET_sales.xlsx", "c:\\olympus\\20230626-160910\\202305_OML_GIET_sales.xlsx");
//        localExcelFiles.put("202305_OKR_GIET_sales.xlsx", "c:\\olympus\\20230626-160910\\202305_OKR_GIET_sales.xlsx");
//        log.info("매출데이터 로컬 PC에 다운로드 완료 : ", localExcelFiles);


        log.info("==> 5단계 : 매출데이터 파일들을 하나로 병합 시작");
        String mergeFilePath = "";
        try {
            mergeFilePath = excelDataMerge(localExcelFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("병합된 파일 위치 : {}", mergeFilePath);
        log.info("==> 5단계 : 매출데이터 파일들을 하나로 병합 종료");

        //Merge file을 쉐어포인트에 업로드 실시
        log.info("=> 6단계 : Merge 엑셀 파일 업로드 시작");
        ms.uploadMergerFileToSharePoint(accessToken, siteId, mergeFilePath, yyyymm);
        log.info("=> 6단계 : Merge 엑셀 파일 업로드 종료");

        log.info("★★★★★★★★프로그램 종료★★★★★★★★");
    }

    private String excelDataMerge(Map<String, String> localExcelFiles) throws IOException {
        String folderPath = localExcelFiles.get("folderPath");
        localExcelFiles.remove("folderPath");

        String baseFile = localExcelFiles.get("baseFilePath");
        localExcelFiles.remove("baseFilePath");

        //엑셀 객체 초기화
        SXSSFWorkbook workbook = new SXSSFWorkbook(500);

        //시트 생성
        SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet("지사 매출데이터 병합");

        //헤더 생성
        log.info("=> 헤더 생성 시작");
        createHeader(sheet, baseFile);
        log.info("=> 헤더 생성 완료");

        //데이터 생성
        log.info("=> 데이터 생성 시작");
        ArrayList<List<String>> data = createDataRow(localExcelFiles);
        log.info("=> 데이터 생성 완료");

        //Merge 엑셀 파일 생성
        log.info("=> Merge 엑셀 파일 생성 시작");
        String mergeFilePath = createMergeFile(folderPath, workbook, sheet, data);
        log.info("=> Merge 엑셀 파일 생성 종료");

        return mergeFilePath;

    }

    private String createMergeFile(String folderPath, SXSSFWorkbook workbook, SXSSFSheet sheet, ArrayList<List<String>> data) throws IOException {

        for (int i = 0; i < data.size(); i++) {
            List<String> rowData = data.get(i);

            SXSSFRow row = (SXSSFRow) sheet.createRow(i + 1);
            for (int j = 0; j < rowData.size(); j++) {
                String cellData = rowData.get(j);
                SXSSFCell cell = (SXSSFCell) row.createCell(j);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(cellData);
            }
        }

        String mergeFilePath = folderPath + "\\매출통합본.xlsx";
        File file = new File(folderPath + "\\매출통합본.xlsx");
        FileOutputStream fileout = new FileOutputStream(file);
        workbook.write(fileout);
        fileout.close();

        log.info("지사별 매출 통합본 파일 위치 : {}", mergeFilePath);
        return mergeFilePath;

    }

    private ArrayList<List<String>> createDataRow(Map<String, String> localExcelFiles) throws IOException {
        ArrayList<List<String>> result = new ArrayList<>();

        Iterator iterator = localExcelFiles.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String filePath = localExcelFiles.get(key);
            String orgId = key.split("_")[1];

            //File 읽기
            log.info("=> 작업 파일 시작 : {}", filePath);
            FileInputStream file = new FileInputStream(filePath);
            XSSFWorkbook excel = new XSSFWorkbook(file);

            XSSFSheet tmpSheet = excel.getSheetAt(0);

            log.info("=> 작업 파일 작업 로우 갯수 : {}", tmpSheet.getLastRowNum());
            for (int i = 1; i < tmpSheet.getLastRowNum() + 1; i++) {
//                log.info("=> 작업 로우 번호 : {}", i);

                List<String> rowData = new ArrayList<>();
                rowData.add(orgId);

                XSSFRow tmpRow = tmpSheet.getRow(i);
                for (int j = 0; j < tmpRow.getLastCellNum(); j++) {
                    XSSFCell tmpCell = tmpRow.getCell(j);

                    try {
                        String value = "";
                        switch (tmpCell.getCellType()) {
                            case XSSFCell.CELL_TYPE_FORMULA:
                                value = tmpCell.getCellFormula();
                                break;
                            case XSSFCell.CELL_TYPE_NUMERIC:
                                if (DateUtil.isCellDateFormatted(tmpCell)) {
                                    // 날짜 형식으로 변환
                                    Date date = tmpCell.getDateCellValue();
                                    // 원하는 날짜 형식으로 포맷팅
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    value = dateFormat.format(date);
                                } else {
                                    value = tmpCell.getNumericCellValue() + "";
                                }

                                break;
                            case XSSFCell.CELL_TYPE_STRING:
                                value = tmpCell.getStringCellValue() + "";
                                break;
                            case XSSFCell.CELL_TYPE_BLANK:
                                value = tmpCell.getBooleanCellValue() + "";
                                break;
                            case XSSFCell.CELL_TYPE_ERROR:
                                value = tmpCell.getErrorCellValue() + "";
                                break;
                        }
                        rowData.add(value);
                    } catch (Exception e) {
                        log.error("=> 에러 발생 !!1");
                        log.info("=> 셀 위치 : (" + i + "," + (j + 1) + "), 셀 타입 : " + tmpCell.getCellType() + ", 셀 값 : " + tmpCell.toString());
                    }

                }
                result.add(rowData);
            }
            log.info("=> 작업 파일 종료 : {}", filePath);
            file.close();
        }
        return result;
    }

    private String cellReader(XSSFCell cell) {
        String value = "";

        switch (cell.getCellType()) {
            case XSSFCell.CELL_TYPE_FORMULA:
                value = cell.getCellFormula();
                break;
            case XSSFCell.CELL_TYPE_NUMERIC:
                value = cell.getNumericCellValue() + "";
                break;
            case XSSFCell.CELL_TYPE_STRING:
                value = cell.getStringCellValue() + "";
                break;
            case XSSFCell.CELL_TYPE_BLANK:
                value = cell.getBooleanCellValue() + "";
                break;
            case XSSFCell.CELL_TYPE_ERROR:
                value = cell.getErrorCellValue() + "";
                break;
        }
        return value;
    }

    private void createHeader(SXSSFSheet sheet, String baseFile) throws IOException {
        SXSSFRow row = (SXSSFRow) sheet.createRow(0);

        //File 읽기
        FileInputStream file = new FileInputStream(baseFile);
        XSSFWorkbook excel = new XSSFWorkbook(file);
        XSSFSheet excelSheet = excel.getSheetAt(0);
        Iterator<Cell> cellIterator = excelSheet.getRow(0).iterator();

        //첫Cell에 Org Id 추가
        Cell cell = row.createCell(0);
        cell.setCellValue("Org Id");

        //나머지 셀 생성
        int i = 0;
        while (cellIterator.hasNext()) {
            Cell tmpCell = cellIterator.next();
            i++;

            cell = row.createCell(i);
            cell.setCellValue(tmpCell.getStringCellValue());
        }
        file.close();
    }

    private Map<String, String> fileUrlDownloadToLocalPath(ArrayList<FolderDTO> folderSubFiles) {
        Map<String, String> localExcelFiles = new HashMap<>();

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        String folderName = new SimpleDateFormat("yyyyMMdd-HHmmss").format(currentTimestamp);

        String path = projectFolder + "\\" + folderName;
        localExcelFiles.put("folderPath", path);
        localExcelFiles.put("baseFilePath", "");

        File Folder = new File(path);

        if (!Folder.exists()) {
            Folder.mkdir();
        }

        for (FolderDTO folder : folderSubFiles) {
            FileDTO file = folder.getFile();
            if (file == null) {
                continue;
            }

            try {
                String filePath = path + "\\" + file.getFileName();
                InputStream in = URI.create(file.getDownloadUrl()).toURL().openStream();
                Files.copy(in, Paths.get(filePath));
                log.info("=> [" + folder.getFolderName() + "/" + file.getFileName() + "] 다운로드 완료 - " + filePath);
                localExcelFiles.put(file.getFileName(), filePath);

                if (file.getFileName().split("_")[1].equals("OKR")) {
                    localExcelFiles.put("baseFilePath", filePath);
                }
            } catch (Exception e) {
                log.info("=> [" + folder.getFolderName() + "/" + file.getFileName() + "] 다운로드 실패 사유 ▼▼▼▼▼▼▼▼▼▼▼▼");
                e.printStackTrace();
            }
        }

        if (localExcelFiles.get("baseFilePath").equals("")) {
            localExcelFiles.put("baseFilePath", path + "\\" + folderSubFiles.get(0).getFile().getFileName());
        }


        return localExcelFiles;
    }



}
