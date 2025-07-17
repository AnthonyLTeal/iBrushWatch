package com.example.testwatch;

public class FeatureContainer {
    private double[] features;
    private int lastIndex = 0;

    public FeatureContainer(int featureCount){
        features = new double[featureCount];
    }

    public void PushFeature(double value) throws ArrayIndexOutOfBoundsException {
        if (lastIndex >= features.length){
            throw new ArrayIndexOutOfBoundsException("Features array is filled.");
        }
        features[lastIndex] = value;
        lastIndex += 1;
    }

    public double[] GetFeatures(){
        return features;
    }
}
