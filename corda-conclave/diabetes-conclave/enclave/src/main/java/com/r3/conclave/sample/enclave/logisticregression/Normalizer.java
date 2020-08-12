package com.r3.conclave.sample.enclave.logisticregression;

import java.util.Arrays;

public class Normalizer {
    private double[] max;
    private double[] min;

    public double[] getMax() {
        return max;
    }

    public double[] getMin() {
        return min;
    }

    public void norm(double[][] rawData) {

        max = Arrays.copyOfRange(rawData[0], 0, rawData[0].length - 1);
        min = Arrays.copyOfRange(rawData[0], 0, rawData[0].length - 1);

        // find min and max of all features
        for (double[] row : rawData) {
            for (int i = 0; i < row.length - 1; i++) {
                if (row[i] > max[i]) {
                    max[i] = row[i];
                }
                if (row[i] < min[i]) {
                    min[i] = row[i];
                }
            }
        }

        for (double[] row : rawData) {
            norm(row);
        }
    }

    public void norm(double[] row) {
        for (int i = 0; i < row.length - 1; i++) {

            // todo: just in case the input data is beyond the range of training data
            if (row[i] <= min[i]) {
                row[i] = 0;
                continue;
            }
            if (row[i] >= max[i]) {
                row[i] = 1;
                continue;
            }
            if (min[i] < max[i]) {
                row[i] = (row[i] - min[i]) / (max[i] - min[i]);
            } else { // min==max
                row[i] = 0;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Utils.arrayToString(sb, max);
        sb.append(Utils.RECORD_SEPARATER);
        Utils.arrayToString(sb, min);

        return sb.toString();
    }

    public static Normalizer fromString(String str){

        String[] data = str.split(Utils.RECORD_SEPARATER);
        String[] max = data[0].split(Utils.ELEMENT_SEPARATER);
        String[] min = data[1].split(Utils.ELEMENT_SEPARATER);

        Normalizer normalizer = new Normalizer();
        normalizer.max = new double[max.length];
        normalizer.min = new double[min.length];
        for (int i = 0; i < max.length; i++) {
            normalizer.max[i] = Double.parseDouble(max[i]);
            normalizer.min[i] = Double.parseDouble(min[i]);
        }
        return normalizer;
    }

}
