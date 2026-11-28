package com.payvault.service;

import com.payvault.exception.*;
import com.payvault.model.Account;
import com.payvault.util.Validator;

import java.math.BigDecimal;
import java.util.*;

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

    public List<Account> getAllAccounts(Comparator<Account> order)
    {
        List<Account> account = new ArrayList<>(accounts.values());

        account.sort(order);

        return account;
    }

    public int removeEmptyAccounts()
    {
        int removed = 0;
        Iterator<Account> it = accounts.values().iterator();
        while (it.hasNext())
        {
            Account account = it.next();
            boolean noMoney = account.getBalance().compareTo(BigDecimal.ZERO) == 0;
            boolean neverUsed = account.getHistory().isEmpty();
            if (noMoney && neverUsed)
            {
                it.remove();
                removed++;
            }
        }
        return removed;
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

    public void transfer(String fromNo, String toNo, BigDecimal amount)
            throws AccountNotFoundException, InvalidTransferException, AccountFrozenException,
            DailyLimitExceededException, InsufficientBalanceException
    {
        amount = Validator.validateAmount(amount);

        Account from = findAccount(fromNo);
        Account to = findAccount(toNo);

        if (fromNo.equals(toNo))
        {
            throw new InvalidTransferException("Cannot transfer to the same account: " + fromNo);
        }

        if (to.isFrozen())
        {
            throw new AccountFrozenException(toNo);
        }

        from.transferOut(amount, toNo);

        to.transferIn(amount, fromNo);
    }
}