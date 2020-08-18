package com.r3.conclave.sample.enclave.logisticregression;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Utils {

    public static String OBJECT_SEPARATER = "\n";
    public static String RECORD_SEPARATER = ";";
    public static String ELEMENT_SEPARATER = ",";

    public static void arrayToString(StringBuilder sb, double[] arr) {
        int i = 0;
        while (true) {
            sb.append(arr[i]);
            if (i == (arr.length - 1)) {
                return;
            }

            sb.append(ELEMENT_SEPARATER);
            ++i;
        }
    }

    public static void printDataset(List<Instance> instances) {
        for (int i = 0; i < instances.size(); i++) {
            double[] x = instances.get(i).getX();
            int label = instances.get(i).getLabel();
            System.out.println("Instance " + i);
            for (double v : x) {
                System.out.print(v + " ");
            }
            System.out.print("Label " + label);
            System.out.println("");
        }

    }

    public static double[][] loadDataFromString(String rawTextData){

        String[] allRecord = rawTextData.split(RECORD_SEPARATER);

        int recordNum = allRecord.length;
        int featuresNum = allRecord[0].split(ELEMENT_SEPARATER).length;
        double[][] dataSet = new double[recordNum][featuresNum];

        for (int i = 0; i < allRecord.length; i++){
            String record = allRecord[i];
            String[] row = record.split(ELEMENT_SEPARATER);
            for (int j = 0; j < featuresNum; j++) {
                String num = row[j];
                dataSet[i][j] = Double.parseDouble(num);
            }
        }
        return dataSet;
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

    public static double[][] loadRawData(String file) throws Exception {

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

    public static List<Instance> readDataSet(String file) throws FileNotFoundException {
        List<Instance> dataset = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(file))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("#")) {
                    continue;
                }
                line = line.replaceAll("\\s+", "");

                String[] columns = line.split(ELEMENT_SEPARATER);

                // skip last column
                double[] data = new double[columns.length - 1];
                for (int i = 0; i < columns.length - 1; i++) {
                    data[i] = Double.parseDouble(columns[i]);
                }
                int label = Integer.parseInt(columns[columns.length - 1]);
                Instance instance = new Instance(label, data);
                dataset.add(instance);

            }
        }
        return dataset;
    }

    public static List<Instance> readDataSet(double[][] rawData) {
        List<Instance> dataset = new ArrayList<>();
        for (double[] row : rawData) {
            double[] data = Arrays.copyOfRange(row, 0, row.length - 1);
            int label = (int) row[row.length - 1];
            Instance instance = new Instance(label, data);
            dataset.add(instance);
        }
        return dataset;
    }

}

