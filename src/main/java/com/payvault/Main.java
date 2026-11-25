package com.payvault;

import com.payvault.model.Transaction;
import com.payvault.model.TransactionType;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        Transaction T1 = new Transaction.Builder().type(TransactionType.DEPOSIT).amount(new BigDecimal("500")).note("salary").build();
        Transaction T2 = new Transaction.Builder().type(TransactionType.TRANSFER_OUT).amount(new BigDecimal("250.5")).counterparty("C001").build();

        System.out.println(T1);
        System.out.println(T2);

        try {
            new Transaction.Builder().type(TransactionType.DEPOSIT).build();
        }
        catch(IllegalStateException e)
        {
            System.out.println("Error: " + e.getMessage());
        }

        Transaction T3 = new Transaction.Builder().type(TransactionType.WITHDRAWAL).amount(new BigDecimal("100")).build();
        System.out.println(T3);

    }
}
