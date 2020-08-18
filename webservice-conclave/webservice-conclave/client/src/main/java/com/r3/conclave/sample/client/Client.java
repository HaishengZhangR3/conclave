package com.r3.conclave.sample.client;

import java.util.Random;

public class Client {

    // file for training
    // diabetes.txt
    // diabetes_2.txt

    // data for predict:
    //10,139,80,0,0,27.1,1.441,57 -> 0
    //1,189,60,23,846,30.1,0.398,59 -> 1
    //1,89,66,23,94,28.1,0.167,21->0
    //0,137,40,35,168,43.1,2.288,33->1
    //5,116,74,0,0,25.6,0.201,30->0
    //3,78,50,32,88,31,0.248,26->1
    //10,115,0,0,0,35.3,0.134,29->0
    //2,197,70,45,543,30.5,0.158,53->1

    public static void main(String[] args) throws Exception {

        // args:
        // 1. [app] training:{file name}
        // 2. [app] predict:{patient data in a string}
        if (args.length < 1) {
            usage();
            System.exit(1);
        }

        String param = args[0];
        System.out.println("Command parameters: " + param);
        String[] commands = param.split(":");
        if (commands[0].equalsIgnoreCase("training")) {
            String algorithm = new String(training(commands[1]));
            System.out.println("Algorithm after training:" + algorithm);
        } else if (commands[0].equalsIgnoreCase("predict")) {
            String result = new String(predict(commands[1]));
            System.out.println(commands[1] + " result:" + result);
        }
    }

    private static void usage() {
        System.out.println("Usage:\n" +
                "Either pass file for training as follows:\n" +
                "[app] training:{file name}\n" +
                "or pass patient data for predicting:\n" +
                "[app] predict:{patient data in a string}\n");
    }

    private static byte[] training(String file) {
        try {
            String data = FileLoader.loadDataFromFile(file);
            return ConclaveConnector.postData(
                    ConclaveConnector.trainingPoint,
                    "training" + new Random().nextInt(1000),
                    0,
                    ("T:"+data).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    private static byte[] predict( String data) {
        try {
            byte[] algorithm = ConclaveConnector.postData(
                    ConclaveConnector.algorithmPoint,
                    "Algorithm" + new Random().nextInt(1000),
                    0,
                    "A:getAlgorithm".getBytes());

            System.out.println("Algorithm to predict:" + new String(algorithm));
            return ConclaveConnector.postData(
                    ConclaveConnector.predictPoint,
                    "predict" + new Random().nextInt(1000),
                    0,
                    ("P:"+new String(algorithm)+ FileLoader.OBJECT_SEPARATER+data).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

}
