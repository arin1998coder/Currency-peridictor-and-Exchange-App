package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.controller;

import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.ExchangeCurrencyRequestDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.ExchangeCurrencyResponseDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.PredictCurrencyResponseDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto.PredictCurrencyValueRequestDTO;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.exception.InvalidDateFormatException;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.exception.NotG10CountryException;
import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.service.CurrencyValueService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/currency")
public class CurrencyValueController {

    @Autowired
    CurrencyValueService currencyValueService;

   // add to db past 30 days currency value api
    @PostMapping("/addtoDB")
    public String addCurrencyToDB() throws IOException {
        return currencyValueService.updateCurrencyValues();
    }

   // predict api
    @GetMapping("/predict")
    public PredictCurrencyResponseDTO predictCurrencyValue(@RequestBody PredictCurrencyValueRequestDTO predictCurrencyValueRequestDTO) throws InvalidDateFormatException {
        return currencyValueService.predict(predictCurrencyValueRequestDTO);
    }

    //Exchange to another currency api
    @GetMapping("/exchange")
    public ResponseEntity exchangeCurrency(@RequestBody ExchangeCurrencyRequestDTO exchangeCurrencyRequestDTO) {
//
        try {
            ExchangeCurrencyResponseDTO exchangeCurrencyResponseDTO = currencyValueService.echangeCurrency(exchangeCurrencyRequestDTO);
            return new ResponseEntity(exchangeCurrencyResponseDTO, HttpStatus.ACCEPTED);
        } catch (NotG10CountryException e) {
            String errorMessage = e.getMessage();
            return new ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST);

        }
    }
}
