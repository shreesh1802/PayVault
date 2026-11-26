package com.payvault.model;

import com.payvault.exception.AccountFrozenException;
import com.payvault.exception.AccountNotFoundException;
import com.payvault.exception.DailyLimitExceededException;
import com.payvault.exception.InsufficientBalanceException;
import com.payvault.util.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Account {
    private final String accountNo;
    private final String holderName;
    private BigDecimal balance;
    private boolean frozen = false;
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("50000.00");


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

    public BigDecimal getTransferredToday()
    {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        LocalDate today = LocalDate.now();
        for(Transaction t : history)
        {
            boolean isTransferOut = t.getType() == TransactionType.TRANSFER_OUT;
            boolean isToday = t.getTimeStamp().toLocalDate().equals(today);
            if(isTransferOut && isToday)
            {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    public void transferOut(BigDecimal amount, String toAccountNo) throws AccountFrozenException,DailyLimitExceededException, InsufficientBalanceException
    {
        amount = Validator.validateAmount(amount);

        checkNotFrozen();
        checkDailyLimit(amount);
        checkSufficientFunds(amount);

        balance = balance.subtract(amount);
        recordTransaction(TransactionType.TRANSFER_OUT, amount, null , toAccountNo);
    }


    public void transferIn(BigDecimal amount, String fromAccountNo) throws AccountFrozenException
    {
        amount = Validator.validateAmount(amount);

        checkNotFrozen();

        balance = balance.add(amount);
        recordTransaction(TransactionType.TRANSFER_IN, amount, null, fromAccountNo);
    }


    public void deposit(BigDecimal amount) throws AccountFrozenException
    {
        deposit(amount, null);
    }

    public void deposit(BigDecimal amount, String note) throws AccountFrozenException
    {
        amount = Validator.validateAmount(amount);

        checkNotFrozen();

        balance = balance.add(amount);
        recordTransaction(TransactionType.DEPOSIT, amount, note);
    }


    public void withdraw(BigDecimal amount) throws AccountFrozenException, InsufficientBalanceException
    {
        amount = Validator.validateAmount(amount);

        checkNotFrozen();
        checkSufficientFunds(amount);

        balance = balance.subtract(amount);
        recordTransaction(TransactionType.WITHDRAWAL, amount, null);
    }


    public abstract BigDecimal withdrawableAmount();

    private void recordTransaction(TransactionType type,BigDecimal amount, String note,String counterparty)
    {
        Transaction t = new Transaction.Builder().type(type).amount(amount).note(note).counterparty(counterparty).build();
        history.add(t);
    }

    private void recordTransaction(TransactionType type, BigDecimal amount, String note)
    {
        recordTransaction(type, amount, note, null);
    }

    private void checkNotFrozen() throws AccountFrozenException
    {
        if (frozen)
        {
            throw new AccountFrozenException(accountNo);
        }
    }

    private void checkSufficientFunds(BigDecimal amount) throws InsufficientBalanceException
    {
        BigDecimal available = withdrawableAmount();
        if (amount.compareTo(available) > 0)
        {
            throw new InsufficientBalanceException(accountNo, amount.subtract(available));
        }
    }

    private void checkDailyLimit(BigDecimal amount) throws DailyLimitExceededException
    {
        BigDecimal sentToday = getTransferredToday();
        if (sentToday.add(amount).compareTo(DAILY_TRANSFER_LIMIT) > 0)
        {
            throw new DailyLimitExceededException("Daily transfer limit ₹" + DAILY_TRANSFER_LIMIT
                    + " exceeded for " + accountNo + ": already sent ₹" + sentToday
                    + " today, tried ₹" + amount);
        }
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