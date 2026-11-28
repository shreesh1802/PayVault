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
import com.payvault.model.TransactionType;
import com.payvault.service.StatementService;
import com.payvault.service.WalletService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Main
{
    public static void main(String[] args) throws WalletException
    {
        runM2Checks();
        System.out.println();
        runM3Checks();
        System.out.println();
        runM4Checks();
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

    // ==================== M4: collections ====================
    private static void runM4Checks() throws WalletException
    {
        WalletService service = new WalletService();
        service.addAccount(new SavingsAccount("S001", "Aman", new BigDecimal("10000")));
        service.addAccount(new CurrentAccount("C001", "Ritesh", new BigDecimal("5000")));
        service.addAccount(new SavingsAccount("S002", "Priya", new BigDecimal("3000")));

        service.deposit("S001", new BigDecimal("500"), "salary");
        service.withdraw("S001", new BigDecimal("300"));
        service.transfer("S001", "C001", new BigDecimal("1500"));
        service.transfer("S001", "S002", new BigDecimal("700"));
        service.deposit("S001", new BigDecimal("2500"), "bonus");
        service.transfer("S001", "C001", new BigDecimal("300"));

        Account s001 = service.findAccount("S001");

        System.out.println("=== M4: Collections ===");

        // 1. Balance and history size
        System.out.println("1. " + s001 + " | history entries: " + s001.getHistory().size());

        // 2. Last 3 transactions, newest first (ArrayDeque)
        System.out.println("2. Last 3 transactions:");
        printTransactions(s001.getRecentTransaction(3));

        // 3. Asking for more than exist
        System.out.println("3. getRecentTransactions(10) returned " + s001.getRecentTransaction(10).size() + " entries");

        // 4. Sorted by amount, largest first; equal amounts older first (Comparator chain)
        System.out.println("4. History by amount:");
        printTransactions(s001.getHistoryByAmount());

        // 5. The original history is untouched by the sort
        System.out.println("5. getHistory() still starts with: " + s001.getHistory().get(0));

        // 6. Newest first (anonymous Comparator class)
        System.out.println("6. Newest transaction: " + s001.getHistoryNewestFirst().get(0));

        // 7. Unique counterparties (HashSet)
        Set<String> counterparties = s001.getCounterParties();
        System.out.println("7. Counterparties: " + counterparties + " | size: " + counterparties.size()
                + " | contains C001: " + counterparties.contains("C001")
                + " | contains X999: " + counterparties.contains("X999"));

        // 8. Monthly statement (TreeMap): months come out sorted although added out of order
        List<Transaction> backdated = List.of(
                backdatedTransaction(TransactionType.DEPOSIT, "1000", LocalDateTime.of(2026, 11, 5, 10, 0)),
                backdatedTransaction(TransactionType.WITHDRAWAL, "200", LocalDateTime.of(2026, 9, 12, 10, 0)),
                backdatedTransaction(TransactionType.DEPOSIT, "300", LocalDateTime.of(2026, 11, 20, 10, 0)),
                backdatedTransaction(TransactionType.DEPOSIT, "50", LocalDateTime.of(2026, 10, 1, 10, 0)),
                backdatedTransaction(TransactionType.TRANSFER_OUT, "100", LocalDateTime.of(2026, 9, 30, 10, 0)));

        TreeMap<YearMonth, List<Transaction>> statement = new TreeMap<>(StatementService.groupByMonth(backdated));
        System.out.println("8. Monthly statement:");
        for (Map.Entry<YearMonth, List<Transaction>> entry : statement.entrySet())
        {
            System.out.println("      " + entry.getKey() + " -> " + entry.getValue().size() + " transaction(s)");
        }

        // 9. First and last month
        System.out.println("9. First month: " + statement.firstKey() + " | last month: " + statement.lastKey());

        // 10. All accounts by balance, largest first (Comparator passed as a parameter)
        System.out.println("10. Accounts by balance:");
        for (Account acc : service.getAllAccounts(Comparator.comparing(Account::getBalance).reversed()))
        {
            System.out.println("      " + acc);
        }

        // 11. All accounts by account number
        System.out.println("11. Accounts by number:");
        for (Account acc : service.getAllAccounts(Comparator.comparing(Account::getAccountNo)))
        {
            System.out.println("      " + acc);
        }

        // 12. Remove never-used accounts (Iterator.remove)
        service.addAccount(new SavingsAccount("E001", "Empty One"));
        service.addAccount(new CurrentAccount("E002", "Empty Two"));
        int removed = service.removeEmptyAccounts();
        int left = service.getAllAccounts(Comparator.comparing(Account::getAccountNo)).size();
        System.out.println("12. Removed " + removed + " empty account(s) | accounts left: " + left);
        try
        {
            service.findAccount("E001");
            System.out.println("12. ERROR: E001 still exists");
        }
        catch (AccountNotFoundException e)
        {
            System.out.println("12. " + e.getMessage());
        }

        // 13. equals / hashCode: same account number = same account
        Account first = new SavingsAccount("S001", "Aman", new BigDecimal("5000"));
        Account second = new SavingsAccount("S001", "Someone Else", new BigDecimal("100"));
        Set<Account> set = new HashSet<>();
        set.add(first);
        set.add(second);
        System.out.println("13. equals: " + first.equals(second) + " | ==: " + (first == second)
                + " | HashSet size: " + set.size());

        // 14. Removing inside a for-each loop -> ConcurrentModificationException
        List<String> ids = new ArrayList<>(List.of("S001", "E001", "S002", "E002"));
        try
        {
            for (String id : ids)
            {
                if (id.startsWith("E"))
                {
                    ids.remove(id);
                }
            }
            System.out.println("14. ERROR: no exception, list is " + ids);
        }
        catch (ConcurrentModificationException e)
        {
            System.out.println("14. ConcurrentModificationException caught | list is now: " + ids);
        }

        // 15. The safe way: Iterator.remove()
        ids = new ArrayList<>(List.of("S001", "E001", "S002", "E002"));
        Iterator<String> it = ids.iterator();
        while (it.hasNext())
        {
            if (it.next().startsWith("E"))
            {
                it.remove();
            }
        }
        System.out.println("15. Iterator.remove() | list is now: " + ids);

        // 16. runM2Checks() and runM3Checks() above still print what they did before
    }

    // ==================== helpers ====================
    private static void printTransactions(List<Transaction> transactions)
    {
        for (Transaction t : transactions)
        {
            System.out.println("      " + t);
        }
    }

    private static Transaction backdatedTransaction(TransactionType type, String amount, LocalDateTime when)
    {
        return new Transaction.Builder()
                .type(type)
                .amount(new BigDecimal(amount))
                .timeStamp(when)
                .build();
    }
}