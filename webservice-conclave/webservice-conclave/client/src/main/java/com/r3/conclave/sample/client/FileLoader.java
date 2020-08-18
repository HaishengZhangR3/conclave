package com.r3.conclave.sample.client;

import java.io.File;
import java.util.Scanner;

public class FileLoader {

    public static String OBJECT_SEPARATER = "\n";
    public static String RECORD_SEPARATER = ";";
    public static String ELEMENT_SEPARATER = ",";

    public static String loadDataFromFile(String file) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Scanner scanner = new Scanner(new File(file));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                stringBuilder.append(line).append(RECORD_SEPARATER);
            }
            return stringBuilder.toString();
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }
}

