package com.r3.conclave.sample.enclave.logisticregression;

import java.util.*;

public class Main {

    public static void main(String... args) throws Exception {
        test();
    }

    public static void test() throws Exception {


        // TODO: Those are the data to pass back to client:
        // classifier (weights or called coefficients), normalizer (min and max array)

        CrossValidation lrm = new CrossValidation();

        final String path = "/";
        String file = path + "diabetes.txt";

        // data format: n features + 1 class(0/1)
        double[][] dataSet = Utils.loadDataFromResource(file);
        Normalizer norm = new Normalizer();
        norm.norm(dataSet);

        if (dataSet.length == 0) {
            System.out.println("-------------- Empty file, exit --------------");
            return;
        }
        // prepare parameters for CV
        int features = dataSet[0].length - 1;
        int restarts = 0;
        int foldsNum = 5;
        int epochs = 500;
        double learningRate = 0.15;

        double maxAccuracy = 0;
        Classifier bestClassifier = new Classifier(0);

        for (int i = 0; i < 1000; i++) {
            //result = Accuracy, Sensitivity, Specificity, Positive Predictive Value, Negative Predictive Value
            double[] result = lrm.training(dataSet, restarts, foldsNum, epochs, learningRate, features);
            if (result[0] > maxAccuracy) {
                maxAccuracy = result[0];
                bestClassifier = lrm.getClassifier();
                System.out.println("Best result:" + Arrays.toString(result));
                System.out.println("Best classifier:" + bestClassifier.toString());
            }
            // 85 is good enough
            if (maxAccuracy > 85) {
                break;
            }
        }

        System.out.println("max accuracy:" + maxAccuracy);
        System.out.println("classifier:" + bestClassifier.toString());
        System.out.println("normalizer:" + norm.toString());

        System.out.println("******************----------predict------------*****************");
        int correct = 0;
        for (double[] row : dataSet) {
            double[] data = Arrays.copyOfRange(row, 0, row.length - 1);
            int label = (int) row[row.length - 1];

            int predict = lrm.getClassifier().predict(data);
            if (predict == label) {
                correct++;
            }
            // System.out.println("expect:" + label + ", result:" + predict);
        }
        System.out.println("Accuracy:" + 100 * correct / dataSet.length + "%");


    }
}
