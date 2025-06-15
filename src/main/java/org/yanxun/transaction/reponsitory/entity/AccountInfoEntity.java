package org.yanxun.transaction.reponsitory.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户实体
 * @author yanxun
 * @since 2025/6/14 21:07
 */
@Data
public class AccountInfoEntity {

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 币种
     */
    private String currency;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 账户状态，1：正常，2：冻结
     */
    private Integer state;

}
