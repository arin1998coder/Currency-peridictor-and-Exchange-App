# Currency-peridictor-and-Exchange-App

Created a Currency Predictor and Currency Exchange Rate Backend App using JAVA , SpringBoot and JPA-Hibernate.

1.I have a local database named "currency" where i created a "currency_values" table which has the following columns Id , Currency Code, date , value and isG10( if country is G10 then "YES" else "NO").

2. I have used APILayer's Live Currency Value TimeSeries API to fetch the currency values of past 30 days from 18-03-2023 minus 30 days and stored the currency values fetched in my database table.

3.Created a GET API localhost:8090/currency/exchange which supports currency conversion for G10 Currencies. For e.g if User wants to know what is 2 USD in INR he/she can use this endpoint to fetch the converted amount in INR.

4.I have created a ExchangeCurrencyRequest DTO in which user can pass the request to call the exchange API in a JSON format as follows:

{
    "amt":10,
    "baseCurrencyCode":"JPY",
    "destinationCurrencyCode":"INR"
}

and Response is been provided in a ExchangeCurrencyResponseDTO in the below format:

{
    "message": "Amount Exchanged successfuly",
    "amtInBaseCurrency": 10.0,
    "baseCurrency": "JPY",
    "exchangedAmt": 6.2598221366537565,
    "destinationCurrency": "INR"
}

if user wants to convert a currency Code which is not part of G10 country , I have implemented a Custom Exception called NotAG10CountryCurrencyCode which gets thrown during this scenario. for e.g

Below request by user , wanting to convert from INR to USD,
{
    "amt":10,
    "baseCurrencyCode":"INR",
    "destinationCurrencyCode":"USD"
}

Below is the response of the API, SInce INR is not a G10 country

Country code is not G10: INR

5.Implemented a predict API , that uses the ARMA model approach based on TimeSeries and Historical data processing to predict a Currency value for a Future date.
The Results are not very accurate i feel because i had to google and use chat gpt to extract the code for the predict logic as it uses machine learning and other mathematical stuffs which i am not aware off.

But Somehow it provides a prediction based on the past 30 days currency value of a particular currency code for a User provided date.

Lets say user provides a date : 2023-03-25 and currency Code : INR

What this API will do is, it fetches the last 30 days INR value from the db and store it in a List. And based on this values it uses a ARMA(autoregressive–moving-average) Predictor logic to provide a prediction for the date 2023-03-25.

But again i ran some tests and i feel the results are not accurate.

