package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.predictor;


import java.time.LocalDate;
import java.util.List;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public class ARIMAPredictor {

    public static double predict(List<Double> currencyValues, LocalDate futureDate) {
        double[] currencyArray = currencyValues.stream().mapToDouble(Double::doubleValue).toArray();
        double[] diffArray = difference(currencyArray);
        int p = 2; // number of AR terms
        int d = 1; // order of differencing
        int q = 0; // number of MA terms
        int n = diffArray.length;
        double[] forecast = new double[n + 1];
        // initialize forecast array with known values
        System.arraycopy(diffArray, 0, forecast, 0, n);
        // calculate forecast for future date
        double[] coef = computeARIMACoefficients(diffArray, p, d, q);
        for (int i = n; i < forecast.length; i++) {
            forecast[i] = coef[0];
            for (int j = 1; j <= p; j++) {
                forecast[i] += coef[j] * forecast[i - j];
            }
        }
        // invert differencing to get forecasted value for future date
        double predictedDiff = forecast[forecast.length - 1];
        for (int i = n - 1; i >= 0; i--) {
            predictedDiff += forecast[i + 1] - diffArray[i];
        }
        double predictedValue = currencyArray[currencyArray.length - 1] + predictedDiff;
        return predictedValue;
    }

    private static double[] difference(double[] values) {
        double[] diff = new double[values.length - 1];
        for (int i = 0; i < diff.length; i++) {
            diff[i] = values[i + 1] - values[i];
        }
        return diff;
    }

    private static double[] computeARIMACoefficients(double[] values, int p, int d, int q) {
        double[] arimaValues = new double[values.length - d];
        System.arraycopy(values, d, arimaValues, 0, arimaValues.length);
        double[][] X = new double[arimaValues.length - p][p];
        double[] y = new double[arimaValues.length - p];
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < p; j++) {
                X[i][j] = arimaValues[i + p - j - 1];
            }
            y[i] = arimaValues[i + p];
        }
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, X);
        double[] coef = regression.estimateRegressionParameters();
        double[] fullCoef = new double[p + q + 1];
        fullCoef[0] = 1;
        System.arraycopy(coef, 0, fullCoef, 1, p);
        return fullCoef;
    }
}
