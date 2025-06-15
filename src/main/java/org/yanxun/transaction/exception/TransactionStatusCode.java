package org.yanxun.transaction.exception;

/**
 * 交易异常错误码
 */
public enum TransactionStatusCode {

    //交易成功
    TRANSACTION_SUCCESS(1, "交易成功"),
    TRANSACTION_NOT_FOUND(1001, "交易不存在"),
    INVALID_TRANSACTION_DATA(1002, "交易数据无效"),
    TRANSACTION_CREATE_FAILED(1003, "交易创建失败"),
    ACCOUNT_NOT_FOUND(1004, "账户不存在"),
    ACCOUNT_NOT_ENOUGH_BALANCE(1005, "账户余额不足"),
    TRANSACTION_DUPLICATE(1006, "重复交易"),
    ACCOUNT_STATUS_ERROR(1007, "账户状态异常"),
    TRANSACTION_STATUS_ERROR(1008, "交易状态错误"),
    TRANSACTION_UPDATE_FAILED(1009, "更新交易失败"),
    INVALID_PARAM(1010, "参数不合法"),
    ;

    private final int code;
    private final String message;

    TransactionStatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
