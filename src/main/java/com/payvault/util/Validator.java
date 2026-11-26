package com.payvault.util;

import com.payvault.exception.InvalidAmountException;

import java.math.BigDecimal;

public final class Validator
{
    private Validator() {}

    public static BigDecimal validateAmount(BigDecimal amount)
    {
        if(amount == null)
        {
            throw new InvalidAmountException("invalid amount");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new InvalidAmountException("Amount must be positive: " + amount);
        }
        if(amount.stripTrailingZeros().scale() > 2)
        {
            throw new InvalidAmountException("Amount can have at most 2 decimal places: " + amount);
        }
        return amount.setScale(2);
    }
}
