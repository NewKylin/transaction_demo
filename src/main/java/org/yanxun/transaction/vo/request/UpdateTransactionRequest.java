package org.yanxun.transaction.vo.request;

import lombok.Data;
import org.yanxun.transaction.exception.TransactionException;
import org.yanxun.transaction.exception.TransactionStatusCode;

import java.math.BigDecimal;

/**
 * 更新交易
 * @author yanxun
 * @since 2025/6/15 9:09
 */
@Data
public class UpdateTransactionRequest {


    /**
     * 交易号
     */
    private String transactionNo;

    /**
     * 交易类型（1:支付，2：退款）
     */
    private Integer transactionType;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户ID
     */
    private Long accountInfoId;

    /**
     * 转入卡号
     */
    private String inCardNo;



    public void checkParam(){
        if(transactionNo == null || transactionNo.isEmpty()){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"交易号不能为空");
        }
        if(transactionType == null){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"交易类型不能为空");
        }
        if(amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"交易金额不能小于或者等于0");
        }

        if(currency == null || currency.isEmpty()){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"币种不能为空");
        }
        if(userId == null){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"用户ID不能为空");
        }
        if(accountInfoId == null){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"账户ID不能为空");
        }

        if(inCardNo == null || inCardNo.isEmpty()){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"转入卡号不能为空");
        }
    }
}
