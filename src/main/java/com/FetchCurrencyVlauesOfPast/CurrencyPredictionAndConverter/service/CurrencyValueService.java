package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.service;

import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.ExchangeCurrencyRequestDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.ExchangeCurrencyResponseDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.PredictCurrencyResponseDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.PredictCurrencyValueRequestDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.entity.CurrencyValues;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.exception.InvalidDateFormatException;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.exception.NotG10CountryException;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.predictor.ARIMAPredictor;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.repository.CurrencyValueRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class CurrencyValueService {

    @Autowired
    CurrencyValueRepository currencyValueRepository;

    private OkHttpClient client = new OkHttpClient.Builder().build();

    public ArrayList<String> getDates(LocalDate currentDate) {

         String startingDate="";
         String currDate="";
        // Get the current date
        // Format the current date as a String in "YYYY-MM-DD" format
        currDate = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Calculate the start date (30 days ago from the current date)
        LocalDate startDate = currentDate.minusDays(30);
        // Format the start date as a String in "YYYY-MM-DD" format
         startingDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

         ArrayList<String> dates = new ArrayList<>();
         dates.add(startingDate);
         dates.add(currDate);

         return dates;

    }
//converts stirng to localDate YYYY-MM-DD format
    public LocalDate convertStringToLocalDate(String strDate){
        LocalDate date = LocalDate.parse(strDate);
        return date;
    }
        public String updateCurrencyValues() throws IOException {
            ArrayList<String> dates = getDates(LocalDate.now());
            Request request = new Request.Builder()
                    .url("https://api.apilayer.com/exchangerates_data/timeseries?start_date="+dates.get(0)+"&end_date="+dates.get(1)+"&base=USD") //fetches currency values of past 30 days and stores in db
                    .addHeader("apikey", "yQ3EIlCUwcbb6CFgDxQvWG1ZY9KwJ6DO")
                    .method("GET", null)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                JSONObject myObject = new JSONObject(response.body().string());
//                System.out.println(myObject);
                parseJSONObjectAndStoreInDB(myObject);  //parses the jSON obj and updates the cyrrency values of mentioned date in my sql db

//                List<CurrencyValues> data=getCurrencyValuesSortedByDateDesc();
//                deleteTable(); //deletes the table
////                deleteTable();
//               SortTheCurrencyValuesTable(data);
                return "Currency Values updated to Table Successfully";
            }
        }

//
    //parses the JSON object fetched from live api of all currency code values of past 30 days and stores in the table "currency_values"
        public void parseJSONObjectAndStoreInDB(JSONObject myObject){
            // Retrieving end date
            String endDateString = myObject.getString("end_date");
            LocalDate endDate = LocalDate.parse(endDateString);
            System.out.println("End date: " + endDate);

            // Retrieving currency codes and rates
            JSONObject ratesObject = myObject.getJSONObject("rates");
            Iterator<String> datesIterator = ratesObject.keys();
            while (datesIterator.hasNext()) {
                String date = datesIterator.next();
                JSONObject dateRatesObject = ratesObject.getJSONObject(date);
                Iterator<String> currencyIterator = dateRatesObject.keys();
                while (currencyIterator.hasNext()) {
                    String currencyCode = currencyIterator.next();
                    double rate = dateRatesObject.getDouble(currencyCode);
                    CurrencyValues currencyValues = new CurrencyValues();
                    currencyValues.setDate(date);
                    currencyValues.setValue(rate);
                    currencyValues.setCode(currencyCode);
                    currencyValues.setIsG10(isCountryG10(currencyCode)?"YES":"NO");
                    currencyValueRepository.save(currencyValues);
//                    System.out.println("Date: " + date + ", Currency code: " + currencyCode + ", Rate: " + rate);
                }
            }
        }





    //checks whether a currencycode is a g10 countrie's currency code or not
    public boolean isCountryG10(String currencyCode){

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


    //returns the most recent currency value of a currency code
    @Transactional(readOnly = true)
    public Double findMostRecentByCode(String currencyCode) {
        // Sort the currency values in descending order by date
        Sort sort = Sort.by(Sort.Direction.DESC, "date");

        // Find the most recent currency value for the given currency code
        List<CurrencyValues> currencyValues = currencyValueRepository.findByCode(currencyCode, sort);
        if (currencyValues.isEmpty()) {
            return null;
        } else {
            return currencyValues.get(0).getValue();
        }
    }

    //checks if a currency code is of a G10 country or not
    @Transactional(readOnly = true)
    public boolean isG10Country(String currencyCode) {
        // Check if there is any currency value for the given code with isG10 set to "YES"
        return currencyValueRepository.existsByCodeAndIsG10(currencyCode, "YES");
    }

    //performs the currency exchange from base currency to destination currency and gives the exchanged rate
    public ExchangeCurrencyResponseDTO echangeCurrency(ExchangeCurrencyRequestDTO exchangeCurrencyRequestDTO) throws NotG10CountryException {

        String baseCountryCurrencyCode = exchangeCurrencyRequestDTO.getBaseCurrencyCode();
        double amount = exchangeCurrencyRequestDTO.getAmt();
        String destinationCountryCurrencyCode = exchangeCurrencyRequestDTO.getDestinationCurrencyCode();
        double baseCountryCurrencyValue =0;
        double destinationCountryCurrencyValue=0;
        ExchangeCurrencyResponseDTO exchangeCurrencyResponseDTO = new ExchangeCurrencyResponseDTO();
        double exchangeAmt = 0;

        if(isCountryG10(baseCountryCurrencyCode)){
            //get the currency value of baseCountry's currency and Destination country's currency
            baseCountryCurrencyValue = findMostRecentByCode(baseCountryCurrencyCode);
            destinationCountryCurrencyValue = findMostRecentByCode(destinationCountryCurrencyCode);
            System.out.println(baseCountryCurrencyValue);
            System.out.println(destinationCountryCurrencyValue);
            System.out.println(amount);

            //formula to calculate exchange amt from base to destination
            exchangeAmt = (amount/baseCountryCurrencyValue) * destinationCountryCurrencyValue;
            //set attributes of the Response DTO
            exchangeCurrencyResponseDTO.setBaseCurrency(baseCountryCurrencyCode);
            exchangeCurrencyResponseDTO.setAmtInBaseCurrency(amount);
            exchangeCurrencyResponseDTO.setExchangedAmt(exchangeAmt);
            exchangeCurrencyResponseDTO.setDestinationCurrency(destinationCountryCurrencyCode);
            exchangeCurrencyResponseDTO.setMessage("Amount Exchanged successfuly");
        }
        else{

            throw new NotG10CountryException(baseCountryCurrencyCode);
        }
        return exchangeCurrencyResponseDTO;
    }

    //gives me the past 30 days currency value of the provided currency as a List<Doublne>
    public List<Double> getPast30DaysCurrencyValues(String currencyCode) {
        LocalDate currentDate = LocalDate.now();
        LocalDate pastDate = currentDate.minusDays(29);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<CurrencyValues> currencyValues = currencyValueRepository.findByCodeAndDateBetween(currencyCode, pastDate.format(formatter), currentDate.format(formatter));

        List<Double> values = new ArrayList<>();
        for (CurrencyValues currencyValue : currencyValues) {
            values.add(currencyValue.getValue());
        }
        return values;
    }


    //uses the ArmaPredictor Class to  predict currency value on a particular date
    public PredictCurrencyResponseDTO predictCurrencyOnADay(PredictCurrencyValueRequestDTO predictCurrencyValueRequestDTO) throws InvalidDateFormatException {

        String currencyCode = predictCurrencyValueRequestDTO.getCurrencyCode();
        String dateString = predictCurrencyValueRequestDTO.getDate();
        LocalDate date;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            date = LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException("Please provide the date in YYYY-MM-DD format");
        }

        List<Double> data = getPast30DaysCurrencyValues(currencyCode);

        System.out.println(data);

        ARIMAPredictor predictor = new ARIMAPredictor();
        date = LocalDate.parse(dateString); // replace with your desired date
        double prediction = predictor.predict(data,date);
        System.out.println("Predicted currency value on " + date.toString() + " is " + prediction);

        PredictCurrencyResponseDTO predictCurrencyResponseDTO = new PredictCurrencyResponseDTO();

        predictCurrencyResponseDTO.setPredictedAmt(prediction);

        return predictCurrencyResponseDTO;
    }

}

