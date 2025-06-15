package org.yanxun.transaction;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.yanxun.transaction.service.TransactionService;
import org.yanxun.transaction.vo.common.PageResponse;
import org.yanxun.transaction.vo.request.TransactionPageRequest;
import org.yanxun.transaction.vo.response.Response;
import org.yanxun.transaction.vo.response.TransactionPageResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QueryTransactionTest {

    @Autowired
    private TransactionService transactionService;


    /**
     * 分页查询交易记录
     */
    @Test
    void testPagedData() {

        TransactionPageRequest  request = new TransactionPageRequest();
        request.setPageIndex(1);
        request.setPageSize(10);
        request.setUserId(1001L);
        Response<PageResponse<TransactionPageResponse>> response = transactionService.pageTransaction(request);
        assertEquals(1, response.getCode());

    }
}

