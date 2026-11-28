package com.payvault.service;

import com.payvault.model.Transaction;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatementService
{
    public static Map<YearMonth, List<Transaction>> groupByMonth(List<Transaction> transactions)
    {
        Map<YearMonth, List<Transaction>> byMonth = new TreeMap<>();

        for (Transaction t : transactions)
        {
            YearMonth month = YearMonth.from(t.getTimeStamp());

            List<Transaction> monthList = byMonth.get(month);
            if (monthList == null)
            {
                monthList = new ArrayList<>();
                byMonth.put(month, monthList);
            }
            monthList.add(t);
        }
        return byMonth;
    }
}