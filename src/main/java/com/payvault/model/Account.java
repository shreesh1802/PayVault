package com.payvault.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Account
{
    private final String accountNo;
    private final String holderName;
    private BigDecimal balance;


    private final List<Transaction> history = new ArrayList<>();

    public Account(String accountNo, String holderName)
    {
        this(accountNo, holderName, BigDecimal.ZERO);
    }

    public Account(String accountNo, String holderName, BigDecimal openingBalance)
    {
        this.accountNo = accountNo;
        this.holderName = holderName;
        this.balance = openingBalance;

        if(openingBalance.compareTo(BigDecimal.ZERO) > 0)
        {
            recordTransaction(TransactionType.DEPOSIT,openingBalance, "Opening Balance");
        }
    }

    public void deposit(BigDecimal amount)
    {
        deposit(amount, null);
    }

    public void deposit(BigDecimal amount, String note)
    {
        balance = balance.add(amount);
        recordTransaction(TransactionType.DEPOSIT, amount, note);
    }

    public boolean withdraw(BigDecimal amount)
    {
        if(amount.compareTo(withdrawableAmount()) > 0)
        {
            return false;
        }
        balance = balance.subtract(amount);
        recordTransaction(TransactionType.WITHDRAWAL, amount, null);
        return true;
    }

    public abstract BigDecimal withdrawableAmount();

    public void recordTransaction(TransactionType type,BigDecimal amount, String note)
    {
        Transaction t = new Transaction.Builder().type(type).amount(amount).note(note).build();
        history.add(t);
    }

    public String getAccountNo() { return accountNo; }
    public String getHolderName() { return holderName; }
    public BigDecimal getBalance() { return balance; }

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