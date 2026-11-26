package com.payvault.model;

import com.payvault.exception.AccountFrozenException;
import com.payvault.exception.InsufficientBalanceException;
import com.payvault.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Account {
    private final String accountNo;
    private final String holderName;
    private BigDecimal balance;
    private boolean frozen = false;


    private final List<Transaction> history = new ArrayList<>();

    public Account(String accountNo, String holderName) {
        this(accountNo, holderName, BigDecimal.ZERO);
    }

    public Account(String accountNo, String holderName, BigDecimal openingBalance) {
        this.accountNo = accountNo;
        this.holderName = holderName;

        if (openingBalance.compareTo(BigDecimal.ZERO) != 0) {
            openingBalance = Validator.validateAmount(openingBalance);
        }

        this.balance = openingBalance.setScale(2);

        if (this.balance.compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(TransactionType.DEPOSIT, this.balance, "Opening Balance");
        }
    }

    public void deposit(BigDecimal amount) throws AccountFrozenException
    {
        deposit(amount, null);
    }

    public void deposit(BigDecimal amount, String note) throws AccountFrozenException
    {
        amount = Validator.validateAmount(amount);

        if (frozen)
        {
            throw new AccountFrozenException(accountNo);
        }
        balance = balance.add(amount);
        recordTransaction(TransactionType.DEPOSIT, amount, note);
    }


    public void withdraw(BigDecimal amount) throws AccountFrozenException, InsufficientBalanceException
    {
        amount = Validator.validateAmount(amount);

        if(frozen)
        {

            throw new AccountFrozenException(accountNo);
        }
        BigDecimal available = withdrawableAmount();

        if(amount.compareTo(available) > 0)
        {
            BigDecimal shortfall = amount.subtract(available);
            throw new InsufficientBalanceException(accountNo, shortfall);
        }
        balance = balance.subtract(amount);
        recordTransaction(TransactionType.WITHDRAWAL, amount, null);
    }



    public abstract BigDecimal withdrawableAmount();

    private void recordTransaction(TransactionType type,BigDecimal amount, String note)
    {
        Transaction t = new Transaction.Builder().type(type).amount(amount).note(note).build();
        history.add(t);
    }



    public String getAccountNo() { return accountNo; }
    public String getHolderName() { return holderName; }
    public BigDecimal getBalance() { return balance; }
    public boolean isFrozen() { return frozen; }
    public void freeze() { frozen = true;}
    public void unfreeze() { frozen = false;}


    public List<Transaction> getHistory()
    {
        return Collections.unmodifiableList(history);
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + " " + accountNo + " | " + holderName + " | ₹" + balance.setScale(2, RoundingMode.HALF_UP);
    }
}