package com.payvault.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SavingsAccount extends Account implements InterestBearing
{
    private static final BigDecimal MIN_BALANCE = new BigDecimal("1000");
    private static final BigDecimal ANNUAL_RATE = new BigDecimal("0.04");
    private static final BigDecimal MONTHS = new BigDecimal("12");

    public SavingsAccount(String accountNo, String holderName)
    {
        super(accountNo,holderName);
    }

    public SavingsAccount(String accountNo, String holderName, BigDecimal openingBalance)
    {
        super(accountNo, holderName, openingBalance);
    }

    @Override
    public BigDecimal withdrawableAmount() {

        BigDecimal availableBalance = getBalance().subtract(MIN_BALANCE);
        return availableBalance.max(BigDecimal.ZERO);
    }


    @Override
    public BigDecimal calculateInterest()
    {
        return getBalance().multiply(ANNUAL_RATE).divide(MONTHS, 2 , RoundingMode.HALF_UP);
    }
}
