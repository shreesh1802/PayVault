package com.payvault.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

public class Transaction
{
    private static long nextId = 1;
    private final long id;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String counterparty;
    private final String note;
    private final LocalDateTime timeStamp;

    private Transaction(Builder b)
    {
        this.id = nextId++;
        this.type = b.type;
        this.amount = b.amount;
        this.counterparty = b.counterparty;
        this.note = b.note;
        this.timeStamp = b.timeStamp;
    }

    public long getId() { return id;}
    public TransactionType getType() { return type;}
    public BigDecimal getAmount() { return amount;}
    public String getCounterparty() { return counterparty;}
    public String getNote() { return note;}
    public LocalDateTime getTimeStamp() { return timeStamp;}

    @Override
    public String toString()
    {
        return "#" + id + " " + type + " " + "₹" + amount.setScale(2, RoundingMode.HALF_UP) + (note != null ? " (" + note + ")" : "") + " " + timeStamp.truncatedTo(ChronoUnit.MINUTES);
    }

    public static class Builder
    {
        private TransactionType type;
        private BigDecimal amount;
        private String counterparty;
        private String note;
        private LocalDateTime timeStamp;


        public Builder type(TransactionType type)
        {
            this.type = type;
            return this;
        }

        public Builder amount(BigDecimal amount)
        {
            this.amount = amount;
            return this;
        }
        public Builder counterparty(String counterparty)
        {
            this.counterparty = counterparty;
            return this;
        }

        public Builder note(String note)
        {
            this.note = note;
            return this;
        }

        public Builder timeStamp(LocalDateTime timeStamp)
        {
            this.timeStamp = timeStamp;
            return this;
        }

        public Transaction build()
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
