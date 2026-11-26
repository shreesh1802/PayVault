package com.payvault;

import com.payvault.model.*;


import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Transaction T1 = new Transaction.Builder().type(TransactionType.DEPOSIT).amount(new BigDecimal("500")).note("salary").build();
        Transaction T2 = new Transaction.Builder().type(TransactionType.TRANSFER_OUT).amount(new BigDecimal("250.5")).counterparty("C001").build();

        System.out.println(T1);
        System.out.println(T2);

        try {
            new Transaction.Builder().type(TransactionType.DEPOSIT).build();
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        Transaction T3 = new Transaction.Builder().type(TransactionType.WITHDRAWAL).amount(new BigDecimal("100")).build();
        System.out.println(T3);

        Account a = new Account("T001", "Test", new BigDecimal("1000")) {
            @Override
            public BigDecimal withdrawableAmount() {
                return getBalance();
            }
        };

        a.deposit(new BigDecimal("500"), "salary");
        a.deposit(new BigDecimal("200"));
        System.out.println("Withdraw 5000: " + a.withdraw(new BigDecimal("5000")));
        System.out.println("Withdraw 300:  " + a.withdraw(new BigDecimal("300")));

        System.out.println(a);
        for (Transaction t : a.getHistory()) {
            System.out.println("  " + t);
        }

        try {
            a.getHistory().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("History is read-only");
        }

        SavingsAccount s001 = new SavingsAccount("S001", "Aman", new BigDecimal("5000"));
        System.out.println("1. " + s001 + " | withdrawable: " + s001.withdrawableAmount());

        CurrentAccount c001 = new CurrentAccount("C001", "Ritesh", new BigDecimal("2000"));
        System.out.println("2. " + c001 + " | withdrawable: " + c001.withdrawableAmount());

        boolean ok3 = s001.withdraw(new BigDecimal("4500"));
        System.out.println("3. Withdraw 4500 from S001: " + ok3 + " | balance: " + s001.getBalance());

        boolean ok4 = c001.withdraw(new BigDecimal("6500"));
        System.out.println("4. Withdraw 6500 from C001: " + ok4 + " | balance: " + c001.getBalance() + " | withdrawable: " + c001.withdrawableAmount());

        s001.deposit(new BigDecimal("1000"), "salary");
        System.out.println("5. " + s001 + " | history entries: " + s001.getHistory().size());
        for (Transaction t : s001.getHistory()) {
            System.out.println("     " + t);
        }

        System.out.println("6. S001 monthly interest: " + s001.calculateInterest());

        List<Account> accounts = List.of(s001, c001);
        System.out.println("7. Polymorphism");
        for (Account acc : accounts) {
            System.out.println("    " + acc.getAccountNo() + "withdrawable: " + acc.withdrawableAmount());
        }

        try {
            new Transaction.Builder().type(TransactionType.DEPOSIT).build();
        } catch (IllegalStateException e) {
            System.out.println("8. Caught IllegalStateException: " + e.getMessage());
        }

        try {
            Transaction fake = new Transaction.Builder().type(TransactionType.DEPOSIT).amount(new BigDecimal("100000")).build();
            s001.getHistory().add(fake);
        } catch (UnsupportedOperationException e) {
            System.out.println("9. Caught UnsupportedOperationException: history is read only");
        }
    }
}
