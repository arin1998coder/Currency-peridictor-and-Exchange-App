package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.repository;

import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.entity.CurrencyValues;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CurrencyValueRepository extends JpaRepository<CurrencyValues,Long> {
   @Query(value="select value from currency_values where code=:curr order by this_date desc limit 1",nativeQuery = true)
   Double getMostRecentCurrencyValue(String curr);
   boolean existsByCodeAndIsG10(String currencyCode, String isG10);
//
//    List<CurrencyValues> findByCodeAndDateBetween(String currencyCode, String startDate, String endDate);

    @Query(value = "select distinct(this_date) from currency_values", nativeQuery = true)
    List<String> getDatesForMe();
    @Query(value = "select value from currency_values where code=:curr", nativeQuery = true)
    List<Double> getValuesForMe(String curr);
}
