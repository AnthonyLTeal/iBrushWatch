package com.example.testwatch;

public class PeakMagnitude {
    private int peakIntervals;
    private float averageMagnitude;

    public PeakMagnitude(int _peakIntervals, float _averageMagnitude){
        peakIntervals = _peakIntervals;
        averageMagnitude = _averageMagnitude;
    }

    public int PeakIntervals(){
        return peakIntervals;
    }

    private float AverageMagnitude(){
        return averageMagnitude;
    }
}
