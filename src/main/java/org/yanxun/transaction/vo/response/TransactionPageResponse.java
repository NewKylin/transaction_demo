package org.yanxun.transaction.vo.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yanxun
 * @since 2025/6/14 21:35
 */
@Data
@Builder
public class TransactionPageResponse {

    private Long id;

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
     * 交易状态（1：处理中，2：成功，3：失败）
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
     * 交易流水列表
     */
    private List<FundFlowResponse> fundFlowList;
}
