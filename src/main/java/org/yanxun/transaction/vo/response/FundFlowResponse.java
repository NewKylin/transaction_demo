package org.yanxun.transaction.vo.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author yanxun
 * @since 2025/6/14 21:36
 */
@Data
@Builder
public class FundFlowResponse {

    private Long id;

    /**
     * 交易流水金额
     */
    private BigDecimal amount;

    /**
     * 交易流水币种
     */
    private String currency;

    /**
     * 交易流水类型（1：支出，2：收入）
     */
    private Integer directionType;

    /**
     * 交易前金额
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后金额
     */
    private BigDecimal balanceAfter;

    /**
     * 卡号
     */
    private String cardNo;


    /**
     * 建议账户ID
     */
    private Long accountInfoId;
}
