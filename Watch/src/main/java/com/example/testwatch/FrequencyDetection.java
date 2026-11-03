package com.example.testwatch;

import java.util.ArrayList;

public class FrequencyDetection {
    public static ArrayList<PeakMagnitude> CalculatePeakMagnitudes(ArrayList<Float[]> data){
        Float[] lastPeak = data.get(0);
        int iterationsSinceLast = 0;
        int cooldown = 3;
        ArrayList<PeakMagnitude> peakMagnitudes = new ArrayList<>();
        float totalMagnitude = 0;
        float lastMag = (float)Math.sqrt(lastPeak[0] * lastPeak[0] + lastPeak[1] * lastPeak[1] + lastPeak[2] * lastPeak[2]);
        totalMagnitude += lastMag;

        for (int i = 1; i < data.size(); i++) {
            Float[] vector = data.get(i);
            float magnitude = (float)Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
            totalMagnitude += magnitude;
            iterationsSinceLast += 1;

            if (iterationsSinceLast < cooldown)
                continue;

            float[] v1 = new float[] {vector[0], vector[1], vector[2]};
            float[] v2 = new float[] {lastPeak[0], lastPeak[1], lastPeak[2]};

            float dot = (v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2]) / (magnitude * lastMag);
            if (dot < -0.5f){
                lastPeak = vector;
                lastMag = magnitude;
                peakMagnitudes.add(new PeakMagnitude(iterationsSinceLast, totalMagnitude/iterationsSinceLast));
                totalMagnitude = 0;
                iterationsSinceLast = 0;
            }
        }

        if (iterationsSinceLast > 0){
            peakMagnitudes.add(new PeakMagnitude(iterationsSinceLast, totalMagnitude/iterationsSinceLast));
        }

        return peakMagnitudes;
    }

    public static ArrayList<Float[]> SmoothBackward(ArrayList<Float[]> data, int windowSize) {
        ArrayList<Float[]> smoothedData = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            float x = 0, y = 0, z = 0;

            int start = Math.max(0, i - windowSize);
            int count = 0;

            for (int j = start; j <= i; j++) {
                Float[] current = data.get(j);
                x += current[0];
                y += current[1];
                z += current[2];
                count++;
            }
            smoothedData.add(new Float[]{x / count, y / count, z / count});
        }
        return smoothedData;
    }

    public static FreqMagPlotData GetPlottableDataPoints(ArrayList<PeakMagnitude> peakMagnitudes, int timeWindowMS){
//        frequency_data = []
//        magnitude_data = []
//        current_time = 0
//        time_window = 5000 #in milliseconds
//        interval_count = 0
//        cum_magnitude = 0
//        total_time = 0
//        for i in range(0, len(time_changes)):
//        intervals, magnitude = time_changes[i]
//        current_time += intervals * 16.67
//        total_time += intervals * 16.67
//        interval_count += 1 #this is the actually stroke count
//        cum_magnitude += magnitude
//        if current_time >= time_window:
//        frequency_data.append([interval_count / (time_window / 1000), (total_time) / 1000])
//        magnitude_data.append([cum_magnitude / interval_count, (total_time) / 1000])
//        current_time = 0
//        interval_count = 0
//        cum_magnitude = 0
//
//        if interval_count > 0:
//        actual_seconds = current_time / 1000
//        frequency_data.append([interval_count / actual_seconds, total_time / 1000])
//        magnitude_data.append([cum_magnitude / interval_count, total_time / 1000])
        return null;
    }
}
