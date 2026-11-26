package com.payvault.exception;

public class InvalidTransferException extends WalletException {
    public InvalidTransferException(String message)
    {
        super(message);
    }
}
