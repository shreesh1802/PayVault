package com.payvault.model;

import java.math.BigDecimal;

public class CurrentAccount extends Account
{
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("5000");

    public CurrentAccount(String accountNo, String holderName)
    {
        super(accountNo, holderName);
    }

    public CurrentAccount(String accountNo, String holderName, BigDecimal openingBalance)
    {
        super(accountNo,holderName,openingBalance);
    }


    @Override
    public BigDecimal withdrawableAmount()
    {
        return getBalance().add(OVERDRAFT_LIMIT);
    }
}
