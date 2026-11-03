package com.example.testwatch;

import java.util.ArrayList;

public class FreqMagPlotData {
    private ArrayList<Float> magPlotData;
    private ArrayList<Float> freqPlotData;
    private ArrayList<Float> timePoints;

    public FreqMagPlotData(ArrayList<Float> _magPlotData, ArrayList<Float> _freqPlotData, ArrayList<Float> _timePoints){
        magPlotData = _magPlotData;
        freqPlotData = _freqPlotData;
        timePoints = _timePoints;
    }

    ArrayList<Float> MagPlotData(){
        return magPlotData;
    }

    ArrayList<Float> FreqPlotData(){
        return freqPlotData;
    }

    ArrayList<Float> TimePoints(){
        return timePoints;
    }
}
