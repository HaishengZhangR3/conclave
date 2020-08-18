package com.r3.conclave.sample.enclave;

import com.r3.conclave.common.enclave.EnclaveCall;
import com.r3.conclave.enclave.Enclave;
import com.r3.conclave.mail.EnclaveMail;
import com.r3.conclave.mail.MutableMail;
import com.r3.conclave.sample.enclave.logisticregression.*;

import java.util.Arrays;

/**
 * Simply reverses the bytes that are passed in.
 */
public class DiabetesEnclave extends Enclave implements EnclaveCall {

    // TODO: Those are the data to pass back to client:
    // classifier (weights or called coefficients), normalizer (min and max array)
    private Normalizer normalizer = null;
    private Classifier classifier = null;

    // allTrainingDataSet acts as a DataBase which saves all of the training data from all sources, no normalization.
    private double[][] allTrainingDataSet = null;
    private int local = 0;

    @Override
    public byte[] invoke(byte[] bytes) {
        byte[] result = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = bytes[bytes.length - 1 - i];
        }
        return result;
    }

    @Override
    protected void receiveMail(long id, EnclaveMail mail) {
        System.out.println("enclave ID:" + this.toString());

        byte[] received = mail.getBodyAsBytes();
        String strReceived = new String(received);
        assert strReceived.length() > 2;

        String command = strReceived.substring(0, 1);
        String rawData = strReceived.substring(2);

        MutableMail reply = null;
        if (command.compareToIgnoreCase("T") == 0) {
            reply = handleTraining(mail, rawData);
        } else if (command.compareToIgnoreCase("P") == 0) {
            reply = handlePredict(mail, rawData);
        } else if (command.compareToIgnoreCase("A") == 0) {
            reply = getAlgorithm(mail);
        }
        assert reply != null;
        postMail(reply, null);
    }

    private MutableMail getAlgorithm(EnclaveMail mail) {
        String result = classifier.toString() +
                Utils.OBJECT_SEPARATER +
                normalizer.toString();
        return createMail(mail.getAuthenticatedSender(), result.getBytes());
    }

    private MutableMail handleTraining(EnclaveMail mail, String rawTextData) {
        double[][] dataSet = Utils.loadDataFromString(rawTextData);

        saveTrainingData(dataSet);

        training();

        return getAlgorithm(mail);
    }

    private void saveTrainingData(double[][] newData) {
        int recordNum = newData.length;
        int featureNum = newData[0].length;

        if (allTrainingDataSet != null) {
            recordNum += allTrainingDataSet.length;
        }
        double[][] tempAllTrainingDataSet = new double[recordNum][featureNum];
        for (int i = 0; i < newData.length; i++) {
            tempAllTrainingDataSet[i] = Arrays.copyOf(newData[i], newData[i].length);
        }

        if (allTrainingDataSet != null) {
            for (int i = 0; i < allTrainingDataSet.length; i++) {
                tempAllTrainingDataSet[i + newData.length] = Arrays.copyOf(allTrainingDataSet[i], allTrainingDataSet[i].length);
            }
        }

        allTrainingDataSet = tempAllTrainingDataSet;
        System.out.println("All trainng data set size:" + recordNum + " X " + featureNum);
    }

    private void training() {

        if (allTrainingDataSet == null || allTrainingDataSet.length == 0) {
            return;
        }

        // make a new copy for training since they'd be modified for normalization
        double[][] dataSet = new double[allTrainingDataSet.length][allTrainingDataSet[0].length];
        for (int i = 0; i < allTrainingDataSet.length; i++) {
            dataSet[i] = Arrays.copyOf(allTrainingDataSet[i], allTrainingDataSet[i].length);
        }

        CrossValidation lrm = new CrossValidation();
        normalizer = new Normalizer();
        normalizer.norm(dataSet);

        // prepare parameters for CV
        int features = dataSet[0].length - 1;
        int restarts = 0;
        int foldsNum = 5;
        int epochs = 500;
        double learningRate = 0.15;

        double maxAccuracy = 0;

        for (int i = 0; i < 1000; i++) {

            //result = Accuracy, Sensitivity, Specificity, Positive Predictive Value, Negative Predictive Value
            double[] result = lrm.training(dataSet, restarts, foldsNum, epochs, learningRate, features);
            if (result[0] > maxAccuracy) {
                maxAccuracy = result[0];
                classifier = lrm.getClassifier();
            }
            // 85 is good enough
            System.out.println("the " + i + "th run, accuracy:" + result[0] + "%");
            if (maxAccuracy > 82.5) {
                break;
            }
        }

        assert (normalizer != null && classifier != null);
    }

    private MutableMail handlePredict(EnclaveMail mail, String rawData) {

        // raw data contains:
        //     classifier.toString()
        //     Utils.RECORD_SEPARATER
        //     normalizer.toString()
        //     Utils.RECORD_SEPARATER
        //     testDataArray.toString()
        String[] rawDataList = rawData.split(Utils.OBJECT_SEPARATER);
        System.out.println("Algorithm and data to predict:\n" + rawData);

        assert rawDataList.length == 3;

        classifier = Classifier.fromString(rawDataList[0]);
        normalizer = Normalizer.fromString(rawDataList[1]);
        double[] dataSet = Utils.loadDataFromString(rawDataList[2])[0];

        int category = predict(dataSet);

        String result = Integer.toString(category);
        return createMail(mail.getAuthenticatedSender(), result.getBytes());
    }

    private int predict(double[] data) {
        normalizer.norm(data);
        return classifier.predict(data);
    }

}
