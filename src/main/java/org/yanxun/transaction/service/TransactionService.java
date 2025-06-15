package org.yanxun.transaction.service;

import org.yanxun.transaction.reponsitory.entity.AccountInfoEntity;
import org.yanxun.transaction.vo.common.PageResponse;
import org.yanxun.transaction.vo.request.CreateTransactionRequest;
import org.yanxun.transaction.vo.request.TransactionPageRequest;
import org.yanxun.transaction.vo.request.UpdateTransactionRequest;
import org.yanxun.transaction.vo.response.Response;
import org.yanxun.transaction.vo.response.TransactionPageResponse;

/**
 * 交易服务层
 * @author yan xun
 * @since 2025/6/14 21:51
 */
public interface TransactionService {

    /**
     * 新增交易
     *
     * @param request 新增交易请求
     */
    Response<?> createTransaction(CreateTransactionRequest request);

    /**
     * 修改交易
     * @param request 修改请求
     * @return 修改结果
     */
    Response<?> updateTransaction(UpdateTransactionRequest request);

    /**
     * 删除交易
     * @param transactionNo 交易号
     * @return 修改结果
     */
    Response<?> deleteTransaction(String transactionNo);

    /**
     * 查询交易
     * @param request 查询请求
     * @return 查询结果
     */
    Response<PageResponse<TransactionPageResponse>> pageTransaction(TransactionPageRequest request);

    /**
     * 获取账户信息
     * @param accountId
     * @return
     */
    Response<AccountInfoEntity> getAccountInfo(Long accountId);
}
