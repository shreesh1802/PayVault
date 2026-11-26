package com.payvault.exception;

public class AccountFrozenException extends WalletException
{
    public AccountFrozenException(String accountNo)
    {
        super("Account " + accountNo + " is frozen");
    }
}
