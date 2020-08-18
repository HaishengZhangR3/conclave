package com.r3.conclave.sample.enclave.logisticregression;

public class Instance {
    public int label;
    public double[] x;

    public Instance(int label, double[] x) {
        this.label = label;
        this.x = x;
    }

    public double[] getX() {
        return this.x;
    }

    public int getLabel() {
        return this.label;
    }

    public int getElementSize() {
        return this.x.length;
    }

}
