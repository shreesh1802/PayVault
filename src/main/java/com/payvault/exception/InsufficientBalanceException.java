package com.payvault.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends WalletException
{
    private final BigDecimal shortfall;

    public InsufficientBalanceException(String accountNo, BigDecimal shortfall)
    {
        super("Insufficient Balance in: " + accountNo + " " + "falling short by: ₹" + shortfall);
        this.shortfall = shortfall;
    }

    public BigDecimal getShortfall()
    {
        return shortfall;
    }
}
