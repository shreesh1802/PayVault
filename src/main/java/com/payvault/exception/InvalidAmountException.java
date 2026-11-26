package com.payvault.exception;

public class InvalidAmountException extends RuntimeException
{
    public InvalidAmountException(String message)
    {
        super(message);
    }
}
