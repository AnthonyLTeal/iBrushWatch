package com.example.testwatch;

import android.util.Log;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 *
 */
public class FeatureExtractor {

    public static double signalMagnitudeArea(double[] data) {
        int n = data.length;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            sum += Math.abs(data[i]);
        }
        return sum / n;
    }

    public static double rootMeanSquare(double[] data) {
        double sumSq = 0.0;
        for (double x : data) {
            sumSq += x * x;
        }
        return Math.sqrt(sumSq / data.length);
    }

    static double meanAbsoluteDeviation(double[] data) {
        double mean = StatUtils.mean(data);
        double sum = 0.0;
        for (double x : data) sum += Math.abs(x - mean);
        return sum / data.length;
    }

//    public static double zeroCrossingRate(double[] data) {
//        int count = 0;
//        for (int i = 1; i < data.length; i++) {
//            if ((data[i - 1] >= 0 && data[i] < 0) || (data[i - 1] < 0 && data[i] >= 0)) {
//                count++;
//            }
//        }
//        return (double) count / (data.length - 1);
//    }

    public static double zeroCrossingRate(double[] data, double mean){
        int len = data.length;
        int[] signBits = new int[len];
        for (int i = 0; i < len; i++) {
            signBits[i] = (data[i] - mean) < 0 ? 1 : 0;
        }

        // Step 3: Compute np.diff() and count non-zero diffs
        int signChanges = 0;
        for (int i = 1; i < len; i++) {
            int diff = signBits[i] - signBits[i - 1];
            if (diff != 0) {
                signChanges++;
            }
        }

        // Step 4: Normalize
        return (double) signChanges / len;
    }

    // Correlation between axes
    public static double[] Corr(double[][] segment){
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[][] acc = new double[3][segment.length];
        double[][] gyro = new double[3][segment.length];
        for (int i = 0; i < segment.length; i++) {
            for (int j = 0; j < 3; j++) {
                acc[j][i] = segment[i][j];
                gyro[j][i] = segment[i][j + 3];
            }
        }

        double[] accCorr = new double[3];
        double[] gyroCorr = new double[3];
        int idx = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 3; j++) {
                accCorr[idx] = pc.correlation(acc[i], acc[j]);
                gyroCorr[idx] = pc.correlation(gyro[i], gyro[j]);
                idx++;
            }
        }

        Log.d("INFO", String.format("Corr: %s %s %s %s %s %s", accCorr[0], gyroCorr[0], accCorr[1], gyroCorr[1], accCorr[2], gyroCorr[2]));

        return new double[]{ accCorr[0], gyroCorr[0], accCorr[1], gyroCorr[1], accCorr[2], gyroCorr[2]};
    }

    public static double[] Features(double[][] segment)
    {
        //for now there will always be 6 columns since we only have 6 points of data after preprocessing
        //this might need to be variable in the future (maybe use segment[0].length)
        double[][] columns = new double[6][segment.length];
        for (int i = 0; i < segment.length; i ++){
            for (int j = 0; j < segment[i].length; j++){
                columns[j][i] = segment[i][j];
            }
        }

        for (int i = 0; i < columns.length; i ++){
            double[] data = columns[i];
            double min = StatUtils.min(data);
            double max = StatUtils.max(data);

            double mean = StatUtils.mean(data);
            double variance = StatUtils.populationVariance(data); //need to use population variance here to match ddof=0 (population variance) in numpy since that's what we used to train
            double mad = meanAbsoluteDeviation(data);
            double rms = rootMeanSquare(data);
            double zcr = zeroCrossingRate(data, mean); //this isn't actually being used in the training, might need to revisit this to make sure it's correct but for now we can implement it the same as it's done in the training data
            double iqr = new Percentile().evaluate(data, 75) - new Percentile().evaluate(data, 25);//percentile in java is calculated with type 7 interpolation, but in python it defaults to type 6. Need to change python to type 6 and retrain model
            double percentile75 = new Percentile().evaluate(data, 75);//percentile in java is calculated with type 7 interpolation, but in python it defaults to type 6. Need to change python to type 6 and retrain model
            double kurtosis = new Kurtosis().evaluate(data);
            double sma = signalMagnitudeArea(data);
            double mm = max - min;

            if (i == 0){
                for (int j = 0; j < data.length; j++)
                {
                    Log.d("INFO", "" + data[j]);
                }
            }

            Log.d("INFO", String.format("Features: %s %s %s %s %s %s %s %s %s %s", mean, variance, mad, rms, zcr, iqr, percentile75, kurtosis, sma, mm));

//            System.out.printf("Mean: %.3f\nVar: %.3f\nRMS: %.3f\nMAD: %.3f\nMinMax: %.3f\n", mean, variance, rms, mad, mm);
//            System.out.printf("IQR: %.3f\nP75: %.3f\nKurtosis: %.3f\n", iqr, percentile75, kurtosis);
        }

        double[] channelData = {1.0, -2.0, 3.0, -4.0, 5.0, -6.0};
        double mean = StatUtils.mean(channelData);
        double zcr = zeroCrossingRate(channelData, mean);
        Log.d("INFO", "Zero Crossing Rate: " + zcr);

        return new double[0];
    }
}
