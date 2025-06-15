package org.yanxun.transaction.reponsitory.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.yanxun.transaction.reponsitory.entity.TransactionEntity;
import org.yanxun.transaction.reponsitory.TransactionRepository;
import org.yanxun.transaction.vo.request.TransactionPageRequest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * 交易仓储层
 * @author yanxun
 * @since 2025/6/14 21:13
 */
@Service
public class TransactionRepositoryImpl implements TransactionRepository {

    public static final List<TransactionEntity> TRANSACTION_LIST = new CopyOnWriteArrayList<>();

    @Override
    public void newTransaction(TransactionEntity transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        TRANSACTION_LIST.add(transaction);
    }

    @Override
    public TransactionEntity getTransaction(String transactionNo) {

        return TRANSACTION_LIST.stream()
                .filter(p -> StringUtils.equals(p.getTransactionNo(),  transactionNo))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateTransaction(TransactionEntity transaction) {

        if (transaction == null || StringUtils.isBlank(transaction.getTransactionNo())) {
            throw new IllegalArgumentException("id不能为空");
        }

        TransactionEntity oldTransaction = TRANSACTION_LIST.stream()
                .filter(p -> StringUtils.equals(p.getTransactionNo(),transaction.getTransactionNo()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到该交易记录"));

        //先删除再新增
        TRANSACTION_LIST.remove(oldTransaction);
        //增加新的交易记录
        TRANSACTION_LIST.add(transaction);
    }

    @Override
    public void deleteTransaction(String transactionNo) {

        if (transactionNo == null) {
            throw new IllegalArgumentException("id不能为空");
        }
        TRANSACTION_LIST.removeIf(p -> StringUtils.equals(transactionNo, p.getTransactionNo()));
    }

    @Override
    public Long countTransaction(TransactionPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Predicate<TransactionEntity> predicate = getTransactionEntityPredicate(request);
        return TRANSACTION_LIST.stream().filter(predicate).count();
    }

    /**
     * 获取交易记录查询条件
     * @param request
     * @return
     */
    private Predicate<TransactionEntity> getTransactionEntityPredicate(TransactionPageRequest request) {
        Predicate<TransactionEntity> predicate = p -> true;

        if (StringUtils.isNotBlank(request.getTransactionNo())) {
            predicate = predicate.and(p -> p.getTransactionNo().equals(request.getTransactionNo()));
        }
        if (request.getTransactionType() != null) {
            predicate = predicate.and(p -> p.getTransactionType().equals(request.getTransactionType()));
        }
        if (request.getState() != null) {
            predicate = predicate.and(p -> p.getState().equals(request.getState()));
        }
        if (request.getUserId() != null) {
            predicate = predicate.and(p -> p.getUserId().equals(request.getUserId()));
        }
        return predicate;
    }

    @Override
    public List<TransactionEntity> queryTransaction(TransactionPageRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        Predicate<TransactionEntity> predicate = getTransactionEntityPredicate(request);

        return TRANSACTION_LIST.stream().filter(predicate)
                .skip((long) (request.getPageIndex() - 1) * request.getPageSize())
                .limit(request.getPageSize()).toList();
    }
}
