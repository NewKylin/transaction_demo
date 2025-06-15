package org.yanxun.transaction.reponsitory;

import org.yanxun.transaction.reponsitory.entity.TransactionEntity;
import org.yanxun.transaction.vo.request.TransactionPageRequest;

import java.util.List;

/**
 * 交易仓储层
 * @author yanxun
 * @since 2025/6/14 21:09
 */
public interface TransactionRepository {

    /**
     * 新建交易
     * @param transaction 交易实体
     */
    void newTransaction(TransactionEntity transaction);

    /**
     * 更新交易
     * @param transaction 更新交易
     */
    void updateTransaction(TransactionEntity transaction);

    /**
     * 获取交易
     * @param transactionNo 交易编号
     * @return 交易实体
     */
    TransactionEntity getTransaction(String transactionNo);

    /**
     * 删除交易
     *
     * @param transactionNo 交易ID
     */
    void deleteTransaction(String transactionNo);

    /**
     * 查询交易总数
     * @param request 查询参数
     * @return 交易列表
     */
    Long countTransaction(TransactionPageRequest request);

    /**
     * 分页查询交易记录
     * @param request 查询参数
     * @return 交易记录
     */
    List<TransactionEntity> queryTransaction(TransactionPageRequest request);

}
