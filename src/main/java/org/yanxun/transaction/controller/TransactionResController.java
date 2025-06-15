package org.yanxun.transaction.controller;

import org.springframework.web.bind.annotation.*;
import org.yanxun.transaction.service.TransactionService;
import org.yanxun.transaction.vo.common.PageResponse;
import org.yanxun.transaction.vo.request.CreateTransactionRequest;
import org.yanxun.transaction.vo.request.TransactionPageRequest;
import org.yanxun.transaction.vo.request.UpdateTransactionRequest;
import org.yanxun.transaction.vo.response.Response;
import org.yanxun.transaction.vo.response.TransactionPageResponse;

import java.util.UUID;

/**
 * 交易接口
 * @author yanxun
 * @since 2025/6/15 9:59
 */
@RestController
@RequestMapping("/transaction")
public class TransactionResController {

    private final TransactionService transactionService;

    public TransactionResController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * 创建交易
     * @param request 创建交易请求
     */
    @PostMapping("/create")
    public Response<?> createTransaction(@RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    /**
     * 修改交易
     * @param request 修改交易请求
     */
    @PostMapping("/update")
    public Response<?> updateTransaction(@RequestBody UpdateTransactionRequest request) {
        return transactionService.updateTransaction(request);
    }

    /**
     * 删除交易
     * @param transactionNo 交易编号
     */
    @DeleteMapping("/delete/{transactionNo}")
    public Response<?> deleteTransaction(@PathVariable String transactionNo) {
        return transactionService.deleteTransaction(transactionNo);
    }

    /**
     * 分页查询交易
     * @param request 请求参数
     * @return 分页结果
     */
    @PostMapping("/query/page")
    public Response<PageResponse<TransactionPageResponse>> pageTransaction(@RequestBody TransactionPageRequest request){
        return transactionService.pageTransaction(request);
    }

    @GetMapping("/account/{id}")
    public Response<?> getAccountInfo(@PathVariable Long id) {
        return transactionService.getAccountInfo(id);
    }
}
