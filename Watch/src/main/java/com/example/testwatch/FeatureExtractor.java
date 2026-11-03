package com.example.testwatch;

import android.util.Log;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.stat.regression.SimpleRegression;

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

        //Log.d("INFO", String.format("Corr: %s %s %s %s %s %s", accCorr[0], gyroCorr[0], accCorr[1], gyroCorr[1], accCorr[2], gyroCorr[2]));

        return new double[]{ accCorr[0], gyroCorr[0], accCorr[1], gyroCorr[1], accCorr[2], gyroCorr[2]};
    }

    public static double[] polyfitLinear(double[] channelData) {
        SimpleRegression regression = new SimpleRegression();
        int n = channelData.length;

        // x = 0 to n-2, y = channelData[1] to channelData[n-1]
        for (int i = 0; i < n - 1; i++) {
            regression.addData(i, channelData[i + 1]);
        }

        double slope = regression.getSlope();         // Equivalent to ar_coeffs[0]
        double intercept = regression.getIntercept(); // Equivalent to ar_coeffs[1]
        return new double[] { slope, intercept };
    }

    public static double calculateTiltAngle(double[][] accData) {
        int n = accData.length;

        // Compute mean of each axis (x, y, z)
        double[] accMean = new double[3];
        for (double[] sample : accData) {
            for (int i = 0; i < 3; i++) {
                accMean[i] += sample[i];
            }
        }
        for (int i = 0; i < 3; i++) {
            accMean[i] /= n;
        }

        double accNorm = Math.sqrt(accMean[0] * accMean[0] + accMean[1] * accMean[1] + accMean[2] * accMean[2]);

        if (accNorm > 0) {
            double[] accNormalized = {
                    accMean[0] / accNorm,
                    accMean[1] / accNorm,
                    accMean[2] / accNorm
            };

            // Gravity vector assumed to be [0, 0, 1]
            double dot = accNormalized[2];
            dot = Math.max(-1.0, Math.min(1.0, dot));

            return Math.acos(dot);
        } else {
            return 0;
        }
    }

    public static double[] Features(double[][] segment)
    {
        FeatureContainer featureContainer = new FeatureContainer(103);

        double[] corr = FeatureExtractor.Corr(segment);
        featureContainer.PushFeature(corr[0]);
        featureContainer.PushFeature(corr[1]);
        featureContainer.PushFeature(corr[2]);
        featureContainer.PushFeature(corr[3]);
        featureContainer.PushFeature(corr[4]);
        featureContainer.PushFeature(corr[5]);

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
            featureContainer.PushFeature(mean);

            double variance = StatUtils.populationVariance(data); //need to use population variance here to match ddof=0 (population variance) in numpy since that's what we used to train
            featureContainer.PushFeature(variance);

            double mad = meanAbsoluteDeviation(data);
            featureContainer.PushFeature(mad);

            double rms = rootMeanSquare(data);
            featureContainer.PushFeature(rms);

            double zcr = zeroCrossingRate(data, mean); //this isn't actually being used in the training, might need to revisit this to make sure it's correct but for now we can implement it the same as it's done in the training data
            featureContainer.PushFeature(zcr);

            double iqr = new Percentile().evaluate(data, 75) - new Percentile().evaluate(data, 25);//percentile in java is calculated with type 7 interpolation, but in python it defaults to type 6. Need to change python to type 6 and retrain model
            featureContainer.PushFeature(iqr);

            double percentile75 = new Percentile().evaluate(data, 75);//percentile in java is calculated with type 7 interpolation, but in python it defaults to type 6. Need to change python to type 6 and retrain model
            featureContainer.PushFeature(percentile75);

            double kurtosis = new Kurtosis().evaluate(data);//bias defaults to true in python, need to change that in python and retrain model
            featureContainer.PushFeature(kurtosis);

            double sma = signalMagnitudeArea(data);
            featureContainer.PushFeature(sma);

            double mm = max - min;
            featureContainer.PushFeature(mm);

            //Log.d("INFO", String.format("Features: %s %s %s %s %s %s %s %s %s %s", mean, variance, mad, rms, zcr, iqr, percentile75, kurtosis, sma, mm));
            double[] spectralFeatures = SpectralFeatures.computeFFTFeatures(data);
            featureContainer.PushFeature(spectralFeatures[0]);
            featureContainer.PushFeature(spectralFeatures[1]);
            featureContainer.PushFeature(spectralFeatures[2]);
            featureContainer.PushFeature(spectralFeatures[3]);

            double[] arCoeffs = polyfitLinear(data);
            featureContainer.PushFeature(arCoeffs[0]);
            featureContainer.PushFeature(arCoeffs[1]);

            //Log.d("INFO", String.format("AR1: %s | AR2: %s", arCoeffs[0], arCoeffs[1]));

        }

        double tiltAngle = calculateTiltAngle(segment);
        featureContainer.PushFeature(tiltAngle);

        //Log.d("INFO", String.format("Tilt Angle: %s", tiltAngle));


        return featureContainer.GetFeatures();
    }
}
