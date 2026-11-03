package com.example.testwatch;

import android.util.Log;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.*;
import org.jtransforms.fft.DoubleFFT_1D;

public class SpectralFeatures {

    public static double[] computeFFTFeatures(double[] channelData) {
        int n = channelData.length;

        // Interleaved format: [real0, imag0, real1, imag1, ..., realN-1, imagN-1]
        double[] fftData = new double[2 * n];
        for (int i = 0; i < n; i++) {
            fftData[2 * i] = channelData[i];
            fftData[2 * i + 1] = 0.0;
        }

        // Perform FFT
        DoubleFFT_1D fft = new DoubleFFT_1D(n);
        fft.complexForward(fftData);

        // Frequency bins matching np.fft.fftfreq(n)
        double[] freqs = new double[n];
        for (int i = 0; i < n; i++) {
            freqs[i] = i < n / 2 ? (double) i / n : (double) (i - n) / n;
        }

        // Set range: skip DC (i=0) and Nyquist (i=n/2) if n even
        int upperLimit = (n % 2 == 0) ? n / 2 - 1 : n / 2;
        int binCount = upperLimit;

        double[] powerSpectrum = new double[binCount];
        double[] posFreqs = new double[binCount];

        for (int i = 1; i <= upperLimit; i++) {
            double real = fftData[2 * i];
            double imag = fftData[2 * i + 1];
            double magSq = real * real + imag * imag;

            posFreqs[i - 1] = freqs[i];
            powerSpectrum[i - 1] = magSq;
        }

        // Spectral Energy
        double spectralEnergy = 0.0;
        for (double p : powerSpectrum) {
            spectralEnergy += p;
        }

        // Normalized Power Spectrum
        double[] normPower = new double[binCount];
        for (int i = 0; i < binCount; i++) {
            normPower[i] = powerSpectrum[i] / (spectralEnergy + 1e-10);
        }

        // Spectral Entropy
        double spectralEntropy = 0.0;
        for (double p : normPower) {
            if (p > 0) {
                spectralEntropy -= p * (Math.log(p) / Math.log(2));
            }
        }

        // Spectral Centroid
        double centroidNumerator = 0.0;
        for (int i = 0; i < binCount; i++) {
            centroidNumerator += posFreqs[i] * normPower[i];
        }
        double spectralCentroid = centroidNumerator;

        // Principal Frequency
        int maxIdx = 0;
        double maxPower = powerSpectrum[0];
        for (int i = 1; i < binCount; i++) {
            if (powerSpectrum[i] > maxPower) {
                maxPower = powerSpectrum[i];
                maxIdx = i;
            }
        }
        double principalFreq = posFreqs[maxIdx];
        return new double[]{spectralEnergy, spectralEntropy, spectralCentroid, principalFreq };

        // Output (or store them as needed)
//        Log.d("INFO", "Spectral Energy: %.5f " + spectralEnergy);
//        Log.d("INFO", "Spectral Entropy: %.5f " +  spectralEntropy);
//        Log.d("INFO", "Spectral Centroid: %.5f " +  spectralCentroid);
//        Log.d("INFO", "Principal Frequency: %.5 " + principalFreq);
    }
}