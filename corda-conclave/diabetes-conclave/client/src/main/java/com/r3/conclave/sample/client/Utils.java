package com.r3.conclave.sample.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utils {

    public static String OBJECT_SEPARATER = "\n";
    public static String RECORD_SEPARATER = ";";
    public static String ELEMENT_SEPARATER = ",";

    public static String loadTextFromResource(String file) throws Exception {
        InputStream is = Utils.class.getResourceAsStream(file);
        InputStreamReader streamReader = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(streamReader);

        // line separater: ;
        // feature separater: ,
        StringBuilder stringBuilder = new StringBuilder();
        for (String line; (line = reader.readLine()) != null; ) {
            stringBuilder.append(line).append(Utils.RECORD_SEPARATER);
        }
        return stringBuilder.toString();
    }

    public static double[][] loadDataFromResource(String file) throws Exception {

        List<double[]> rawDataList = new ArrayList<>();

        InputStream is = Utils.class.getResourceAsStream(file);
        InputStreamReader streamReader = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(streamReader);
        for (String line; (line = reader.readLine()) != null; ) {

                if (line.startsWith("#")) {
                    continue;
                }
                line = line.replaceAll("\\s+", "");

                String[] columns = line.split(ELEMENT_SEPARATER);

                // skip last column as label
                double[] data = new double[columns.length];
                for (int i = 0; i < columns.length - 1; i++) {
                    data[i] = Double.parseDouble(columns[i]);
                }
                data[columns.length - 1] = Integer.parseInt(columns[columns.length - 1]);
                rawDataList.add(data);
        }

        int lineNumber = rawDataList.size();
        int columnNumber = rawDataList.get(0).length;
        double[][] rawData = new double[lineNumber][columnNumber];
        for (int n = 0; n < lineNumber; n++) {
            double[] line = rawDataList.get(n);
            rawData[n] = line;
        }
        return rawData;
    }

    public static double[][] loadData(String file) throws Exception {

        List<double[]> rawDataList = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#")) {
                    continue;
                }
                line = line.replaceAll("\\s+", "");

                String[] columns = line.split(ELEMENT_SEPARATER);

                // skip last column as label
                double[] data = new double[columns.length];
                for (int i = 0; i < columns.length - 1; i++) {
                    data[i] = Double.parseDouble(columns[i]);
                }
                data[columns.length - 1] = Integer.parseInt(columns[columns.length - 1]);
                rawDataList.add(data);
            }
        }

        int lineNumber = rawDataList.size();
        int columnNumber = rawDataList.get(0).length;
        double[][] rawData = new double[lineNumber][columnNumber];
        for (int n = 0; n < lineNumber; n++) {
            double[] line = rawDataList.get(n);
            rawData[n] = line;
        }
        return rawData;
    }
}

