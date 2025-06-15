package org.yanxun.transaction.reponsitory;

import org.yanxun.transaction.reponsitory.entity.AccountInfoEntity;

/**
 * @author yan xun
 * @since 2025/6/14 22:20
 */
public interface AccountInfoRepository {

    AccountInfoEntity getAccountInfo(Long accountId);

    /**
     *  通过卡号查询账户信息
     * @param cardNo 卡号
     * @return 账户信息
     */
    AccountInfoEntity getAccountInfoByCardNo(String cardNo);
}
