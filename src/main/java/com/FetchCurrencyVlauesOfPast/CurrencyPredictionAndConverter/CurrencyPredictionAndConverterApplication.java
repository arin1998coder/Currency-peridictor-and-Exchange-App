package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CurrencyPredictionAndConverterApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyPredictionAndConverterApplication.class, args);
	}
}
