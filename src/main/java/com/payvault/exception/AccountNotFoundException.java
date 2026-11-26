package com.payvault.exception;

public class AccountNotFoundException extends WalletException {

    private final String accountNo;

    public AccountNotFoundException(String accountNo) {
        super("Account not found: " + accountNo);
        this.accountNo = accountNo;
    }

    public String getAccountNo() {
        return accountNo;
    }
}

