package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.entity;//import java.io.*;
//import okhttp3.*;
//
//public class main {
//    public static void main(String []args) throws IOException{
//        OkHttpClient client = new OkHttpClient().newBuilder().build();
//
//        Request request = new Request.Builder()
//                .url("https://api.apilayer.com/exchangerates_data/timeseries?start_date=2023-03-13&end_date=2023-03-20")
//                .addHeader("apikey", "yQ3EIlCUwcbb6CFgDxQvWG1ZY9KwJ6DO")
//                .method("GET", })
//            .build();
//    Response response = client.newCall(request).execute();
//    System.out.println(response.body().string());
//}
//}

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "currency_values")
public class CurrencyValues {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String date;

    private Double value;

    private String isG10;

    // getters and setters
}

