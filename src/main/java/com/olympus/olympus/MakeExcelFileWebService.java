package com.olympus.olympus;

import com.olympus.olympus.msGraphAPI.dto.FileDTO;
import com.olympus.olympus.msGraphAPI.dto.FolderDTO;
import com.olympus.olympus.msGraphAPI.service.MsGraphApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
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
public class MakeExcelFileWebService {

    @Value("${projectFolder}")
    private String projectFolder;


    public String execute(Map<String, String> localExcelFiles) {

//        log.info("==> 1단계 : 작업 폴더 생성 시작");
//        String folderPath = createWorkFolder();
//        localExcelFiles.put("folderPath", folderPath);
//        log.info("==> 1단계 : 작업 폴더 생성 종료");


        log.info("==> 2단계 : 매출데이터 파일들을 하나로 병합 시작");
        String mergeFilePath = "";
        try {
            mergeFilePath = excelDataMerge(localExcelFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("병합된 파일 위치 : {}", mergeFilePath);
        log.info("==> 2단계 : 매출데이터 파일들을 하나로 병합 종료");

        return mergeFilePath;
    }

    private String excelDataMerge(Map<String, String> localExcelFiles) throws IOException {
        String folderPath = localExcelFiles.get("folderPath");
        localExcelFiles.remove("folderPath");

        String baseFile = localExcelFiles.get("baseFile");
        localExcelFiles.remove("baseFile");

        String okrFile = localExcelFiles.get("OKR");
        okrFile = folderPath + "\\" + okrFile;

        //데이터 생성
        log.info("=> 데이터 생성 시작");
        ArrayList<List<String>> data = createDataRow(folderPath, localExcelFiles);
        log.info("=> 데이터 생성 완료");

        //Merge 엑셀 파일 생성
        log.info("=> Merge 엑셀 파일 생성 시작");
        String mergeFilePath = createMergeFile(folderPath, baseFile, okrFile, data);
        data.clear();
        log.info("=> Merge 엑셀 파일 생성 종료");

        return mergeFilePath;

    }

    private String createMergeFile(String folderPath, String baseFile, String okrFile, ArrayList<List<String>> data) throws IOException {
        String sourceFilePath = baseFile;
        String destinationFilePath = folderPath + "\\masterFile.csv";

        BufferedReader reader = null;
        FileWriter writer = new FileWriter(destinationFilePath, false);


        //헤더 만들기 - OKR 문서 기준으로 만든다.
        createHeader(okrFile, writer);

        //기존 마스터 파일 복사
        if (!sourceFilePath.equals("")) {
            sourceFilePath = folderPath + "\\" + baseFile;
            reader = new BufferedReader(new FileReader(sourceFilePath));
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                if (lineNumber > 1) {
                    writer.write(line);
                    writer.write(System.lineSeparator());
                }
                lineNumber++;
            }
            reader.close();
        }

        //이어서 쓰기
        log.info("총 작업 로우 수 : {}", data.size());
        for (int i = 0; i < data.size(); i++) {
            List<String> rowData = data.get(i);
            String value = String.join(";", rowData);
            value = value;

            writer.write(value);
            writer.write(System.lineSeparator());
        }
        writer.flush();
        writer.close();

        log.info("지사별 매출 통합본 파일 위치 : {}", destinationFilePath);
        return destinationFilePath;

    }

    private void createHeader(String okrFile, FileWriter writer) throws IOException {
        //File 읽기
        FileInputStream file = new FileInputStream(okrFile);
        XSSFWorkbook excel = new XSSFWorkbook(file);
        XSSFSheet excelSheet = excel.getSheetAt(0);
        Iterator<Cell> cellIterator = excelSheet.getRow(0).iterator();

        List<String> header = new ArrayList<>();

        //헤더에 미리 추가하고 싶은 필드, 뒤에 값 넣는 곳이랑 싱크가 맞아야함
        header.add("org_id");

        while (cellIterator.hasNext()) {
            Cell tmpCell = cellIterator.next();
            header.add(tmpCell.getStringCellValue());
        }

        String value = String.join(";", header);
        writer.write(value);
        writer.write(System.lineSeparator());
        file.close();
    }

    private ArrayList<List<String>> createDataRow(String folderPath, Map<String, String> localExcelFiles) throws IOException {
        ArrayList<List<String>> result = new ArrayList<>();

        Iterator iterator = localExcelFiles.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String fileName = localExcelFiles.get(key);
            if (fileName.equals("")) {
                continue;
            }
            String filePath = folderPath + "\\" + fileName;
            String orgId = key;

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
                        rowData.add("");
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

    private void createHeader(Sheet sheet, String baseFile) throws IOException {
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

    private String createWorkFolder() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        String folderName = new SimpleDateFormat("yyyyMMdd-HHmmss").format(currentTimestamp);

        String path = projectFolder + "\\" + folderName;
        File Folder = new File(path);
        if (!Folder.exists()) {
            Folder.mkdir();
        }

        return path;
    }

}
