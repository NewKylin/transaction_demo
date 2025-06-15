package org.yanxun.transaction.exception;

/**
 * 交易异常
 * @author yanxun
 * @since 2025/6/14 22:34
 */
public class TransactionException extends RuntimeException {

    private final TransactionStatusCode errorCode;

    public TransactionException(TransactionStatusCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TransactionException(TransactionStatusCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TransactionException(TransactionStatusCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public TransactionStatusCode getErrorCode() {
        return errorCode;
    }
}
