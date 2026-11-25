package com.payvault.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction
{
    private static long nextId = 1;
    private final long id;
    private final String type;
    private final BigDecimal amount;
    private final int Counterparty;
    private final String note;
    private final LocalDateTime timeStamp;

    private Transaction(Builder b)
    {
        this.id = nextId++;
        this.type = b.type;
        this.amount = b.amount;
        this.Counterparty = b.Counterparty;
        this.note = b.note;
        this.timeStamp = b.timeStamp;
    }

    public long getId() { return id;}
    public String getType() { return type;}
    public BigDecimal getAmount() { return amount;}
    public int getCounterParty() { return Counterparty;}
    public String getNote() { return note;}
    public LocalDateTime getTimeStamp() { return timeStamp;}

    @Override
    public String toString()
    {
        return "#" + id + " " + type + "₹" + amount + " " + timeStamp;
    }

    public static class Builder
    {
        private long id;
        private String type;
        private BigDecimal amount;
        private int Counterparty;
        private String note;
        private LocalDateTime timeStamp;

        public Builder id(long id)
        {
            this.id = id;
            return this;
        }

        public Builder type(String type)
        {
            this.type = type;
            return this;
        }

        public Builder amount(BigDecimal amount)
        {
            this.amount = amount;
            return this;
        }
        public Builder Counterparty(int Counterparty)
        {
            this.Counterparty = Counterparty;
            return this;
        }

        public Builder note(String note)
        {
            this.note = note;
            return this;
        }

        public Builder timeStamp()
        {
            this.timeStamp = timeStamp;
            return this;
        }

        public Transaction Build()
        {
            if(type == null)
            {
                throw new IllegalStateException("type is required");
            }
            if(amount == null)
            {
                throw new IllegalStateException("amount is required");
            }
            if(timeStamp == null)
            {
                timeStamp = LocalDateTime.now();
            }

            return new Transaction(this);
        }
    }
}
