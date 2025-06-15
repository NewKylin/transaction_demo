package org.yanxun.transaction.reponsitory.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易实体
 * @author yanxun
 * @since 2025/6/14 21:01
 */
@Builder
@Data
public class TransactionEntity {


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
     * 交易状态（0:待处理，1：处理中，2：成功，3：失败，4：交易取消）
     */
    private Integer state;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户ID
     */
    private Long accountInfoId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人名
     */
    private String createName;

    /**
     * 更新人名
     */
    private String updateName;

    /**
     * 交易资金流水
     */
    private List<FundFlowEntity> fundFlowList;

}
