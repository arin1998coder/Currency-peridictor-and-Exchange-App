package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.service;

import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.ExchangeCurrencyRequestDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.ExchangeCurrencyResponseDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.PredictCurrencyResponseDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.PredictCurrencyValueRequestDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.entity.CurrencyValues;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.exception.InvalidDateFormatException;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.exception.NotG10CountryException;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.repository.CurrencyValueRepository;
import com.google.gson.Gson;
import okhttp3.*;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;


@Service
public class CurrencyValueService {

    @Autowired
    CurrencyValueRepository currencyValueRepository;

    private OkHttpClient client = new OkHttpClient.Builder().build();

    static class RatesResponse {
        HashMap<String, Double> rates;
    }

    //converts stirng to localDate YYYY-MM-DD format
    public LocalDate convertStringToLocalDate(String strDate) {
        LocalDate date = LocalDate.parse(strDate);
        return date;
    }

    //pushes the past 30 days currency values to the db table,by fetching the live currency value from APILayer.com
    public String updateCurrencyValues() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        LocalDate localDate = LocalDate.parse("2023-03-20");

        int a = 30;

        while (a > 0) {

            Request request = new Request.Builder()
                    .url("https://api.apilayer.com/exchangerates_data/"+localDate+"?base=USD")
                    .addHeader("apikey", "yQ3EIlCUwcbb6CFgDxQvWG1ZY9KwJ6DO")
                    .method("GET", null)
                    .build();

            Response response = client.newCall(request).execute();
            String json = (response.body().string());

            Gson gson = new Gson();
            HashMap<String, Double> ratesMap = new HashMap<>();

            RatesResponse response1 = gson.fromJson(json, RatesResponse.class);
            for (String currencyCode : response1.rates.keySet()) {
                double rate = response1.rates.get(currencyCode);
                ratesMap.put(currencyCode, rate); // adding in hashmap
            }

            for (String s : ratesMap.keySet()) {
                CurrencyValues currencyValues = new CurrencyValues();
                currencyValues.setCode(s);
                currencyValues.setValue(ratesMap.get(s));
                currencyValues.setThis_date(localDate.toString());
                currencyValues.setIsG10((isCountryG10(s)?"YES":"NO"));
                currencyValueRepository.save(currencyValues);
            }

            a--;
            localDate = localDate.minusDays(1);
        }
        return "Updated currency to DB successfully";
    }



        //checks whether a currencycode is a g10 countrie's currency code or not
        public boolean isCountryG10 (String currencyCode){

            Set<String> g10CurrencyCodes = new HashSet<>(Arrays.asList(
                    "EUR", // Belgium, France, Germany, Italy, Netherlands
                    "CAD", // Canada
                    "JPY", // Japan
                    "SEK", // Sweden
                    "CHF", // Switzerland
                    "GBP", // United Kingdom
                    "USD"  // United States of America
            ));

            return g10CurrencyCodes.contains(currencyCode);
        }

        //checks if a currency code is of a G10 country or not
        @Transactional(readOnly = true)
        public boolean isG10Country (String currencyCode){
            // Check if there is any currency value for the given code with isG10 set to "YES"
            return currencyValueRepository.existsByCodeAndIsG10(currencyCode, "YES");
        }

       // performs the currency exchange from base currency to destination currency and gives the exchanged rate
        public ExchangeCurrencyResponseDTO echangeCurrency (ExchangeCurrencyRequestDTO exchangeCurrencyRequestDTO) throws
        NotG10CountryException {

            String baseCountryCurrencyCode = exchangeCurrencyRequestDTO.getBaseCurrencyCode();
            double amount = exchangeCurrencyRequestDTO.getAmt();
            String destinationCountryCurrencyCode = exchangeCurrencyRequestDTO.getDestinationCurrencyCode();
            double baseCountryCurrencyValue = 0;
            double destinationCountryCurrencyValue = 0;
            ExchangeCurrencyResponseDTO exchangeCurrencyResponseDTO = new ExchangeCurrencyResponseDTO();
            double exchangeAmt = 0;

            if (isCountryG10(baseCountryCurrencyCode)) {
                //get the currency value of baseCountry's currency and Destination country's currency
                baseCountryCurrencyValue = currencyValueRepository.getMostRecentCurrencyValue(baseCountryCurrencyCode);
                destinationCountryCurrencyValue = currencyValueRepository.getMostRecentCurrencyValue(destinationCountryCurrencyCode);
                System.out.println(baseCountryCurrencyValue);
                System.out.println(destinationCountryCurrencyValue);
                System.out.println(amount);

                //formula to calculate exchange amt from base to destination
                exchangeAmt = (amount / baseCountryCurrencyValue) * destinationCountryCurrencyValue;
                //set attributes of the Response DTO
                exchangeCurrencyResponseDTO.setBaseCurrency(baseCountryCurrencyCode);
                exchangeCurrencyResponseDTO.setAmtInBaseCurrency(amount);
                exchangeCurrencyResponseDTO.setExchangedAmt(exchangeAmt);
                exchangeCurrencyResponseDTO.setDestinationCurrency(destinationCountryCurrencyCode);
                exchangeCurrencyResponseDTO.setMessage("Amount Exchanged successfuly");
            } else {

                throw new NotG10CountryException(baseCountryCurrencyCode);
            }
            return exchangeCurrencyResponseDTO;
        }

        //predicts the currency value of a currency code for a particular date by using the Linear Regression Model
        public PredictCurrencyResponseDTO predict(@RequestBody PredictCurrencyValueRequestDTO predictCurrencyValueRequestDTO) throws InvalidDateFormatException {
            String curr = predictCurrencyValueRequestDTO.getCurrencyCode();
            String dateString = predictCurrencyValueRequestDTO.getDate();
            LocalDate date;
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                date = LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                throw new InvalidDateFormatException("Please provide the this_date in YYYY-MM-DD format");
            }
            date = LocalDate.parse(dateString); // replace with your desired this_date

            List<String> localDateList = currencyValueRepository.getDatesForMe();
            List<Double> rateList = currencyValueRepository.getValuesForMe(curr);
            System.out.println(localDateList);
            System.out.println(rateList);
            Double dates[] = new Double[localDateList.size()];

            for (int i = 0; i < localDateList.size(); i++) {
                dates[i] = (double) LocalDate.parse(localDateList.get(i)).toEpochDay();
            }

            Double rates[] = new Double[rateList.size()];
            rateList.toArray(rates);
            SimpleRegression regression = new SimpleRegression();
            for (int i = 0; i < localDateList.size(); i++) {
                regression.addData(dates[i], rates[i]);
            }
            PredictCurrencyResponseDTO predictCurrencyResponseDTO = new PredictCurrencyResponseDTO();

            predictCurrencyResponseDTO.setPredictedAmt(regression.predict(date.toEpochDay()));

            return predictCurrencyResponseDTO;


        }

    }

