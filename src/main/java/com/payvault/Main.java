package com.payvault;

import com.payvault.exception.AccountFrozenException;
import com.payvault.exception.AccountNotFoundException;
import com.payvault.exception.InsufficientBalanceException;
import com.payvault.exception.InvalidAmountException;
import com.payvault.exception.WalletException;
import com.payvault.model.CurrentAccount;
import com.payvault.model.SavingsAccount;
import com.payvault.model.Transaction;
import com.payvault.service.WalletService;

import java.math.BigDecimal;

public class Main
{
    public static void main(String[] args) throws WalletException
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
}