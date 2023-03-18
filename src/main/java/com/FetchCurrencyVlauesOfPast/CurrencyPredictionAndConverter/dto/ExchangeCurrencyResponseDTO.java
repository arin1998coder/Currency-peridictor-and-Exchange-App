package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeCurrencyResponseDTO {

    private String message;
    private double amtInBaseCurrency;
    private String baseCurrency;
    private double exchangedAmt;
    private String destinationCurrency;

}
