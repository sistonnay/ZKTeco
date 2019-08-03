package com.zkteco.autk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * author: Created by Ho Dao on 2019/8/3 0003 12:14
 * email: 372022839@qq.com (github: sistonnay)
 */
public class FileUtil {

    public static String getJSON(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        File jsonFile = new File(fileName);
        if (!jsonFile.exists() || jsonFile.isDirectory()) {
            return null;
        }
        FileInputStream inputStream = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            inputStream = new FileInputStream(jsonFile);
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }
}
