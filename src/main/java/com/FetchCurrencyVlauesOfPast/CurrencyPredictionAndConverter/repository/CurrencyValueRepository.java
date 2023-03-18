package com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.repository;

import com.FetchCurrencyVlauesOfPast.CurrencyPredictionAndConverter.entity.CurrencyValues;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CurrencyValueRepository extends JpaRepository<CurrencyValues,Long> {
    List<CurrencyValues> findByCode(String currencyCode, Sort sort);
    boolean existsByCodeAndIsG10(String currencyCode, String isG10);

    List<CurrencyValues> findByCodeAndDateBetween(String currencyCode, String startDate, String endDate);
}
