package com.r3.conclave.sample.enclave.logisticregression;

import java.util.Arrays;
import java.util.List;

public class Classifier {

    // the learning rate
    public double rate;

    // the weight to learn
    public double[] weights;

    public Classifier(int n) {
        this.rate = 0.05;
        weights = new double[n];
    }

    private static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    public void train(List<Instance> instances, int nr_epoch) {//train LR model
        //Initial coeffs
        Arrays.fill(weights, 0);

        for (int n = 0; n < nr_epoch; n++) {
            double error = 0.0;
            for (Instance instance : instances) { //train model, compute error and update weights (coefficients)
                double[] x = instance.x;
                double predicted = classify(x);
                int label = instance.label;
                error = label - predicted;
                //update weights
                weights[0] = weights[0] + rate * error * predicted * (1.0 - predicted);
                for (int j = 0; j < weights.length - 1; j++) {
                    weights[j + 1] = weights[j + 1] + rate * error * predicted * (1.0 - predicted) * x[j];
                }

            }
        }
    }
    public int predict(double[] x) {
        double result = classify(x);
        return result < 0.5 ? 0 : 1;
    }

    public double classify(double[] x) {
        double logit = 0.0;
        logit += weights[0];
        for (int i = 0; i < weights.length - 1; i++) {
            logit += weights[i + 1] * x[i];
        }
        return sigmoid(logit);
    }

    public double classifyTestSet(double[] x, double[] coeffs) {

        double logit = 0.0;
        logit += coeffs[0];
        for (int i = 0; i < coeffs.length - 1; i++) {
            logit += coeffs[i + 1] * x[i];
        }
        return sigmoid(logit);

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rate);
        sb.append(Utils.RECORD_SEPARATER);
        Utils.arrayToString(sb, weights);

        return sb.toString();
    }

    public static Classifier fromString(String str){

        String[] data = str.split(Utils.RECORD_SEPARATER);
        double rate = Double.parseDouble(data[0]);

        String[] weights = data[1].split(Utils.ELEMENT_SEPARATER);

        Classifier classifier = new Classifier(weights.length);
        classifier.rate = rate;
        for (int i = 0; i < weights.length; i++) {
            classifier.weights[i] = Double.parseDouble(weights[i]);
        }
        return classifier;
    }
}
