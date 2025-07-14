package com.example.testwatch;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.*;

public class SpectralFeatures {

    static FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);

    public static Complex[] computeFFT(double[] signal) {
        // Pad signal to power of 2 for best FFT performance
        int n = 1;
        while (n < signal.length) n *= 2;
        double[] padded = new double[n];
        System.arraycopy(signal, 0, padded, 0, signal.length);

        return fft.transform(padded, TransformType.FORWARD);
    }

    public static double spectralEnergy(Complex[] spectrum) {
        double energy = 0.0;
        for (Complex c : spectrum) {
            energy += c.abs() * c.abs(); // magnitude squared
        }
        return energy;
    }

    public static double spectralEntropy(Complex[] spectrum) {
        double totalEnergy = spectralEnergy(spectrum);
        double entropy = 0.0;
        for (Complex c : spectrum) {
            double p = (c.abs() * c.abs()) / totalEnergy;
            if (p > 0) {
                entropy -= p * Math.log(p);
            }
        }
        return entropy;
    }

    public static double spectralCentroid(Complex[] spectrum, double sampleRate) {
        double num = 0.0, denom = 0.0;
        int n = spectrum.length;
        for (int i = 0; i < n; i++) {
            double freq = (sampleRate * i) / n;
            double mag = spectrum[i].abs();
            num += freq * mag;
            denom += mag;
        }
        return (denom == 0) ? 0 : num / denom;
    }

    public static double principalFrequency(Complex[] spectrum, double sampleRate) {
        int maxIndex = 0;
        double maxMag = 0.0;
        for (int i = 0; i < spectrum.length; i++) {
            double mag = spectrum[i].abs();
            if (mag > maxMag) {
                maxMag = mag;
                maxIndex = i;
            }
        }
        return (sampleRate * maxIndex) / spectrum.length;
    }
}
//
//double[] data = {1.2, -0.4, 2.0, -1.5, 0.3, 0.7, -0.9, 1.0};  // Your signal
//double sampleRate = 50.0; // Hz (adjust to your data)
//
//Complex[] spectrum = SpectralFeatures.computeFFT(data);
//
//double energy = SpectralFeatures.spectralEnergy(spectrum);
//double entropy = SpectralFeatures.spectralEntropy(spectrum);
//double centroid = SpectralFeatures.spectralCentroid(spectrum, sampleRate);
//double principalFreq = SpectralFeatures.principalFrequency(spectrum, sampleRate);
//
//System.out.printf("Energy: %.3f\nEntropy: %.3f\nCentroid: %.3f\nPF: %.3f Hz\n",
//                  energy, entropy, centroid, principalFreq);