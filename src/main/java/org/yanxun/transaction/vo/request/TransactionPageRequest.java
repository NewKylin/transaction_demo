package org.yanxun.transaction.vo.request;

import lombok.Data;
import org.yanxun.transaction.vo.common.PageRequest;

/**
 * @author yanxun
 * @since 2025/6/14 21:32
 */
@Data
public class TransactionPageRequest extends PageRequest {

    /**
     * 交易号
     */
    private String transactionNo;

    /**
     * 交易类型（1:支付，2：退款）
     */
    private Integer transactionType;

    /**
     * 交易状态（1:成功，2：失败）
     */
    private Integer state;

    /**
     * 用户ID
     */
    private Long userId;

}
