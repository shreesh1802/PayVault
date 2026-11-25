package com.payvault;

import com.payvault.model.Transaction;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        Transaction T = new Transaction.Builder().id(3).type("DEPOSIT").amount(BigDecimal.valueOf(500)).Build();
        System.out.println(T);
    }
}
