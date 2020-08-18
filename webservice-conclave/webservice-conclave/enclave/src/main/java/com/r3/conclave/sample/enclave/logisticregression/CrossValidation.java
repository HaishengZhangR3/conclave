package com.r3.conclave.sample.enclave.logisticregression;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CrossValidation {
    public static double TP = 0;
    public static double FP = 0;
    public static double TN = 0;
    public static double FN = 0;
    public static double predictedNeg = 0;
    public static double predictedPos = 0;
    public static double expectedNeg = 0;
    public static double expectedPos = 0;
    //Made all the above fields static in order to access them anywhere. I initialize them in each method as neede.


    private Classifier logistic;

    public Classifier getClassifier() {
        return logistic;
    }

    public double testAccuracy(List<Instance> testInstances, double[] predicted_test, boolean isBest) {
        predictedNeg = 0;
        predictedPos = 0;
        expectedNeg = 0;
        expectedPos = 0;
        TP = 0;
        FP = 0;
        TN = 0;
        FN = 0;
        //test accuracy
        double[] catPredicted = new double[predicted_test.length];
        int numOfCorrect = 0;
        for (int i = 0; i < catPredicted.length; i++) {
            if (predicted_test[i] < 0.5) {
                catPredicted[i] = 0;

            } else {
                catPredicted[i] = 1;

            }
        }
        for (int i = 0; i < catPredicted.length; i++) {
            if (catPredicted[i] == testInstances.get(i).label) {
                numOfCorrect++;
                if (isBest) {
                    if (catPredicted[i] == 0) {
                        TN++;
                        predictedNeg++;
                    } else {
                        TP++;
                        predictedPos++;
                    }
                }
            } else {
                if (isBest) {//isBest = true if the sample to be evaluated is a test set
                    if (catPredicted[i] == 0 && testInstances.get(i).label == 1) {
                        FN++; //predictedNeg++;
                    } else {
                        FP++;//predictedPos++;
                    }
                }
            }
            if (testInstances.get(i).label == 0) {
                expectedNeg++;
            } else {
                expectedPos++;
            }
        }

        return 100 * (numOfCorrect / ((double) testInstances.size()));
    }

    public int getMaxIndex(double[] accuracies) {
        int currMaxIndex = 0;
        double currentMax = accuracies[0];
        for (int i = 1; i < accuracies.length; i++) {
            if (accuracies[i] > currentMax) {
                currMaxIndex = i;
                currentMax = accuracies[i];
            }
        }
        return currMaxIndex;
    }

    public double[] training(String filename, int restarts, int foldsNum, int epochs, double learningRate, int numOfWeights) throws FileNotFoundException {
        List<Instance> allData = Utils.readDataSet(filename);
        return training(allData, restarts, foldsNum, epochs, learningRate, numOfWeights);
    }

    public double[] training(double[][] rawData, int restarts, int foldsNum, int epochs, double learningRate, int numOfWeights) {
        List<Instance> allData = Utils.readDataSet(rawData);
        return training(allData, restarts, foldsNum, epochs, learningRate, numOfWeights);
    }

    private double[] training(List<Instance> allData, int restarts, int foldsNum, int epochs, double learningRate, int numOfWeights) {
        double[] maxCoeffs = new double[numOfWeights];
        double[] accuracyPerRunT = new double[restarts + 1];
        double[][] perfomances = new double[restarts + 1][4];
        ArrayList<ArrayList<Double>> counts = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Double>> countsExp = new ArrayList<ArrayList<Double>>();

        for (int i = 0; i < restarts + 1; i++) {//Number of restarts (including initial restart))
            counts.add(new ArrayList<Double>());//for Chi-square tests
            countsExp.add(new ArrayList<Double>()); //for Chi-square tests
        }
        for (int q = 0; q < restarts + 1; q++) {//Run for certain number of restarts
            logistic = new Classifier(numOfWeights);
            logistic.rate = learningRate;

            Collections.shuffle(allData);
            Instance[][] fold = new Instance[foldsNum][(int) (allData.size() / foldsNum)];
            double[][] coeffs = new double[foldsNum][numOfWeights];
            double[] accuracies = new double[foldsNum - 1];
            int count = 0;

            for (int i = 0; i < foldsNum; i++) {
                for (int j = 0; j < (int) (allData.size() / foldsNum); j++) {
                    fold[i][j] = allData.get(count);
                    count++;
                }
            }
            List<Instance> trainFolds = new ArrayList<Instance>();
            List<Instance> validationSet = new ArrayList<Instance>();
            List<Instance> testInstances = new ArrayList<Instance>();

            for (int j = 0; j < (int) (allData.size() / foldsNum); j++) testInstances.add(fold[0][j]);
            double[] predicted_test = new double[(int) (0.4 * ((int) (allData.size() / foldsNum)))];
            double[] predictedTestSet = new double[testInstances.size()];
            for (int i = 0; i < foldsNum - 1; i++) { //cross validation
                for (int j = 0; j < (int) (allData.size() / foldsNum); j++) {
                    if (j < (((int) (allData.size() / foldsNum) - (int) (0.4 * ((int) (allData.size() / foldsNum)))))) {
                        trainFolds.add(fold[i][j]);
                    } else {
                        validationSet.add(fold[i][j]);/**validation set*/
                    }
                }
                logistic.train(trainFolds, epochs);//train using training set
                for (int k = 0; k < (int) (0.4 * ((int) (allData.size() / foldsNum))); k++) {/**40% validation set*/
                    predicted_test[k] = logistic.classify(validationSet.get((k)).getX());/**classify validation Set*/
                }
                double[] finalWeights = logistic.weights;
                for (int c = 0; c < numOfWeights; c++) {
                    coeffs[i][c] = finalWeights[c];/** Save coeff of the current training instances*/
                }


                double accu = testAccuracy(validationSet, predicted_test, false);/**get Accuracy on validation set*/
                accuracies[i] = accu;
                trainFolds.clear();
                validationSet.clear();
            }//End k-fold cv
            int sum = 0;
            for (int a = 0; a < foldsNum - 1; a++) sum += accuracies[a];
            int maxIndex = getMaxIndex(accuracies);//Get index of highest classification accuracy and use it to get the coefficients which obtained it
            maxCoeffs = new double[numOfWeights];
            for (int j = 0; j < numOfWeights; j++)
                maxCoeffs[j] = coeffs[maxIndex][j]; //get best coeffs and use them to classify final test set: testInstances

            for (int k = 0; k < testInstances.size(); k++) {
                predictedTestSet[k] = logistic.classifyTestSet(testInstances.get(k).getX(), maxCoeffs);//Classify test instances using maxCoeffs
            }
            double currentBestSolution = testAccuracy(testInstances, predictedTestSet, true);
            accuracyPerRunT[q] = currentBestSolution;
            perfomances[q][0] = Math.round(100.0 * ((+(TP) / ((double) (TP + FN)))));//sensitivity
            perfomances[q][1] = Math.round(100.0 * ((TN) / (double) (TN + FP)));//Specificity
            perfomances[q][2] = Math.round(100.0 * ((TP) / (double) (TP + FP)));//PPV
            perfomances[q][3] = Math.round(100.0 * ((TN) / (double) (FN + TN)));  //NPV
            // add counts of predictions...// used these counts for Chi-Square tests purposes, not for classification
            counts.get(q).add(predictedNeg);
            counts.get(q).add(predictedPos);
            countsExp.get(q).add(expectedNeg);
            countsExp.get(q).add(expectedPos);
        }//end restarts
        double sensitivitySum = 0;
        double specificitySum = 0;
        double NPVSum = 0;
        double PPVSum = 0;
        double sumT = 0;
        for (int a = 0; a < restarts + 1; a++) {
            sumT += accuracyPerRunT[a];//sum all all test accuracies from each run to take their average as final classification
            sensitivitySum += perfomances[a][0];
            specificitySum += perfomances[a][1];
            NPVSum += perfomances[a][2];
            PPVSum += perfomances[a][3];
        }

        // keep the classifier with best result
        logistic.weights = Arrays.copyOf(maxCoeffs, maxCoeffs.length);

        // DecimalFormat numberFormat = new DecimalFormat("#.000");
        double[] result = new double[5];
        result[0] = sumT / ((double) (restarts + 1));// Classification Accuracy
        result[1] = Math.round(1.0 * (sensitivitySum / (double) (restarts + 1))); // Sensitivity
        result[2] = Math.round(1.0 * (specificitySum / (double) (restarts + 1))); // Specificity
        result[3] = Math.round(1.0 * (NPVSum / (double) (restarts + 1))); //Positive Predictive Value
        result[4] = Math.round(1.0 * (PPVSum / (double) (restarts + 1))); // Negative Predictive Value

        return  result;
    }
}
