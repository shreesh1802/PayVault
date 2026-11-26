package com.payvault;

import com.payvault.exception.AccountFrozenException;
import com.payvault.exception.AccountNotFoundException;
import com.payvault.exception.DailyLimitExceededException;
import com.payvault.exception.InsufficientBalanceException;
import com.payvault.exception.InvalidAmountException;
import com.payvault.exception.InvalidTransferException;
import com.payvault.exception.WalletException;
import com.payvault.model.Account;
import com.payvault.model.CurrentAccount;
import com.payvault.model.SavingsAccount;
import com.payvault.model.Transaction;
import com.payvault.service.WalletService;

import java.math.BigDecimal;

public class Main
{
    public static void main(String[] args) throws WalletException
    {
        runM2Checks();
        System.out.println();
        runM3Checks();
    }

    private static void runM2Checks() throws WalletException
    {
        WalletService service = new WalletService();
        service.addAccount(new SavingsAccount("S001", "Aman", new BigDecimal("5000")));
        service.addAccount(new CurrentAccount("C001", "Ritesh", new BigDecimal("2000")));

        System.out.println("=== M2: Exceptions & WalletService ===");

        try
        {
            service.deposit("S001", new BigDecimal("0"), null);
            System.out.println("1. ERROR: deposit of 0 was accepted");
        }
        catch (InvalidAmountException e)
        {
            System.out.println("1. Deposit 0: " + e.getMessage());
        }

        try
        {
            service.deposit("S001", new BigDecimal("-500"), null);
            System.out.println("2. ERROR: deposit of -500 was accepted");
        }
        catch (InvalidAmountException e)
        {
            System.out.println("2. Deposit -500: " + e.getMessage());
        }


        try
        {
            service.deposit("S001", new BigDecimal("10.555"), null);
            System.out.println("3. ERROR: deposit of 10.555 was accepted");
        }
        catch (InvalidAmountException e)
        {
            System.out.println("3. Deposit 10.555: " + e.getMessage());
        }


        service.deposit("S001", new BigDecimal("10.500"), "small deposit");
        System.out.println("4. Deposit 10.500 accepted | S001 balance: " + service.findAccount("S001").getBalance());


        try
        {
            service.withdraw("S001", new BigDecimal("4500"));
            System.out.println("5. ERROR: withdrawal of 4500 was allowed");
        }
        catch (InsufficientBalanceException e)
        {
            System.out.println("5. " + e.getMessage() + " | shortfall: " + e.getShortfall());
        }


        try
        {
            service.withdraw("C001", new BigDecimal("7000.01"));
            System.out.println("6. ERROR: withdrawal of 7000.01 was allowed");
        }
        catch (InsufficientBalanceException e)
        {
            System.out.println("6. " + e.getMessage() + " | shortfall: " + e.getShortfall());
        }

        service.withdraw("C001", new BigDecimal("7000"));
        System.out.println("7. Withdraw 7000 allowed | C001 balance: " + service.findAccount("C001").getBalance());

        try
        {
            service.deposit("X999", new BigDecimal("100"), null);
            System.out.println("8. ERROR: deposit into X999 succeeded");
        }
        catch (AccountNotFoundException | AccountFrozenException e)
        {
            System.out.println("8. " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        service.freeze("S001");
        try
        {
            service.deposit("S001", new BigDecimal("100"), null);
            System.out.println("9. ERROR: deposit into a frozen account succeeded");
        }
        catch (AccountNotFoundException | AccountFrozenException e)
        {
            System.out.println("9. " + e.getClass().getSimpleName() + ": " + e.getMessage()
                    + " | balance still: " + service.findAccount("S001").getBalance());
        }


        service.unfreeze("S001");
        service.deposit("S001", new BigDecimal("100"), "after unfreeze");
        System.out.println("10. Unfrozen, deposited 100 | S001 balance: " + service.findAccount("S001").getBalance());


        System.out.println("12. S001 history (" + service.findAccount("S001").getHistory().size() + " entries):");
        for (Transaction t : service.findAccount("S001").getHistory())
        {
            System.out.println("      " + t);
        }


        try
        {
            service.withdraw("C001", new BigDecimal("1"));
            System.out.println("13. Withdraw 1 from C001 succeeded");
        }
        catch (InsufficientBalanceException e)
        {
            System.out.println("13. Withdraw 1 from C001 failed: " + e.getMessage());
        }
        finally
        {
            System.out.println("13. (finally) C001 balance: " + service.findAccount("C001").getBalance());
        }

        // 14. Compile-time check: remove "throws WalletException" from main, then uncomment
        //     the line below. javac says: "unreported exception ... must be caught or declared
        //     to be thrown". Put both back afterwards.
        // service.withdraw("S001", new BigDecimal("1"));
    }

    // ==================== M3: transfers & the daily limit ====================
    private static void runM3Checks() throws WalletException
    {
        WalletService service = new WalletService();
        service.addAccount(new SavingsAccount("S001", "Aman", new BigDecimal("60000")));
        service.addAccount(new CurrentAccount("C001", "Ritesh", new BigDecimal("2000")));
        service.addAccount(new SavingsAccount("S002", "Priya", new BigDecimal("5000")));

        Account s001 = service.findAccount("S001");
        Account c001 = service.findAccount("C001");
        Account s002 = service.findAccount("S002");
        BigDecimal totalBefore = s001.getBalance().add(c001.getBalance()).add(s002.getBalance());

        System.out.println("=== M3: Transfers & the daily limit ===");

        // 1. A normal transfer
        service.transfer("S001", "C001", new BigDecimal("10000"));
        System.out.println("1. Transfer 10000 S001 -> C001 | S001: " + s001.getBalance()
                + " | C001: " + c001.getBalance() + " | S001 sent today: " + s001.getTransferredToday());

        // 2. Invalid amount
        try
        {
            service.transfer("S001", "C001", new BigDecimal("-5"));
            System.out.println("2. ERROR: transfer of -5 succeeded");
        }
        catch (InvalidAmountException e)
        {
            System.out.println("2. Transfer -5: " + e.getMessage());
        }

        // 3. Unknown destination
        try
        {
            service.transfer("S001", "X999", new BigDecimal("100"));
            System.out.println("3. ERROR: transfer to X999 succeeded");
        }
        catch (AccountNotFoundException e)
        {
            System.out.println("3. " + e.getMessage() + " | S001 still: " + s001.getBalance());
        }

        // 4. Same account
        try
        {
            service.transfer("S001", "S001", new BigDecimal("100"));
            System.out.println("4. ERROR: transfer to the same account succeeded");
        }
        catch (InvalidTransferException e)
        {
            System.out.println("4. " + e.getMessage());
        }

        // 5. Frozen destination: no money may be lost
        service.freeze("S002");
        try
        {
            service.transfer("S001", "S002", new BigDecimal("100"));
            System.out.println("5. ERROR: transfer into a frozen account succeeded");
        }
        catch (AccountFrozenException e)
        {
            System.out.println("5. " + e.getMessage() + " | S001 still: " + s001.getBalance() + " (no money lost)");
        }
        finally
        {
            service.unfreeze("S002");
        }

        // 6. Over the daily limit by 0.01
        try
        {
            service.transfer("S001", "C001", new BigDecimal("40000.01"));
            System.out.println("6. ERROR: transfer over the daily limit succeeded");
        }
        catch (DailyLimitExceededException e)
        {
            System.out.println("6. " + e.getMessage());
        }

        // 7. Exactly at the limit: allowed
        service.transfer("S001", "C001", new BigDecimal("40000"));
        System.out.println("7. Transfer 40000 S001 -> C001 | S001: " + s001.getBalance()
                + " | C001: " + c001.getBalance() + " | S001 sent today: " + s001.getTransferredToday());

        // 8. Limit reached, even though there is money left
        try
        {
            service.transfer("S001", "C001", new BigDecimal("1"));
            System.out.println("8. ERROR: transfer after reaching the limit succeeded");
        }
        catch (DailyLimitExceededException e)
        {
            System.out.println("8. " + e.getMessage() + " | S001 withdrawable: " + s001.withdrawableAmount());
        }

        // 9. Not enough funds
        try
        {
            service.transfer("S002", "C001", new BigDecimal("4000.01"));
            System.out.println("9. ERROR: transfer without enough funds succeeded");
        }
        catch (InsufficientBalanceException e)
        {
            System.out.println("9. " + e.getMessage() + " | shortfall: " + e.getShortfall());
        }

        // 10. Exactly the withdrawable amount: allowed
        service.transfer("S002", "C001", new BigDecimal("4000"));
        System.out.println("10. Transfer 4000 S002 -> C001 | S002: " + s002.getBalance() + " | C001: " + c001.getBalance());

        // 11. Money is conserved
        BigDecimal totalAfter = s001.getBalance().add(c001.getBalance()).add(s002.getBalance());
        System.out.println("11. Total before: " + totalBefore + " | total after: " + totalAfter
                + " | conserved: " + (totalBefore.compareTo(totalAfter) == 0));

        // 12. Histories: only successful transfers were recorded
        System.out.println("12. Histories:");
        for (Account acc : new Account[] { s001, c001, s002 })
        {
            System.out.println("    " + acc.getAccountNo() + " (" + acc.getHistory().size() + " entries)");
            for (Transaction t : acc.getHistory())
            {
                System.out.println("      " + t);
            }
        }

        // 13. Withdrawals and incoming transfers don't count towards the limit
        service.withdraw("C001", new BigDecimal("100"));
        System.out.println("13. C001 withdrew 100 | C001 sent today: " + c001.getTransferredToday());

        // 14. Opening balances are validated
        try
        {
            new SavingsAccount("S003", "X", new BigDecimal("100.555"));
            System.out.println("14. ERROR: opening balance 100.555 accepted");
        }
        catch (InvalidAmountException e)
        {
            System.out.println("14. Opening balance 100.555: " + e.getMessage());
        }

        // 15. runM2Checks() above still prints exactly what it did at the end of M2
    }
}