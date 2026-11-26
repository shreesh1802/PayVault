package com.payvault.service;

import com.payvault.exception.AccountFrozenException;
import com.payvault.exception.AccountNotFoundException;
import com.payvault.exception.InsufficientBalanceException;
import com.payvault.model.Account;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class WalletService
{
    private final Map<String, Account> accounts = new HashMap<>();

    public void addAccount(Account account) {
        String accountNo = account.getAccountNo();
        if (accounts.containsKey(accountNo)) {
            throw new IllegalArgumentException("Account Already exists: " + accountNo);
        }
        accounts.put(accountNo, account);
    }

    public Account findAccount(String accountNo) throws AccountNotFoundException {
        Account account = accounts.get(accountNo);
        if (account == null) {
            throw new AccountNotFoundException(accountNo);
        }
        return account;
    }

    public void deposit(String accountNo, BigDecimal amount, String note) throws AccountNotFoundException, AccountFrozenException {
        findAccount(accountNo).deposit(amount, note);
    }

    public void withdraw(String accountNo, BigDecimal amount) throws AccountNotFoundException, AccountFrozenException, InsufficientBalanceException {
        findAccount(accountNo).withdraw(amount);
    }

    public void freeze(String accountNo) throws AccountNotFoundException {
        findAccount(accountNo).freeze();
    }

    public void unfreeze(String accountNo) throws AccountNotFoundException {
        findAccount(accountNo).unfreeze();
    }
}