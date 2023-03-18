package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.exception;

public class NotG10CountryException extends Exception{
    private String countryCode;

    public NotG10CountryException(String countryCode) {
        super("Country code is not G10: " + countryCode);
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
