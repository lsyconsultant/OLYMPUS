package com.olympus.olympus;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class MakeExcelFile {

    @Scheduled(fixedDelay = 60000) // 60초마다 실행
    public void execute() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일 HH_mm_ss");
        String strNowDate = simpleDateFormat.format(new Date());
        String path = "c:\\" + strNowDate + ".txt";

        try (
                FileWriter fw = new FileWriter(path);
        ) {
            for (int i = 0; i < 100; i++) {
                fw.write(Integer.toString(i) + '\n');
            }

            System.out.println("Successfully wrote to the file.");
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }


}
