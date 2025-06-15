package org.yanxun.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yanxun.transaction.exception.TransactionStatusCode;
import org.yanxun.transaction.reponsitory.entity.AccountInfoEntity;
import org.yanxun.transaction.reponsitory.entity.TransactionEntity;
import org.yanxun.transaction.reponsitory.impl.AccountInfoRepositoryImpl;
import org.yanxun.transaction.reponsitory.impl.TransactionRepositoryImpl;
import org.yanxun.transaction.service.impl.TransactionServiceImpl;
import org.yanxun.transaction.vo.request.CreateTransactionRequest;
import org.yanxun.transaction.vo.request.UpdateTransactionRequest;
import org.yanxun.transaction.vo.response.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTransactionServiceTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Spy
    private TransactionRepositoryImpl transactionRepository;

    @Spy
    private AccountInfoRepositoryImpl accountInfoRepository;

    private UpdateTransactionRequest validRequest;
    private TransactionEntity existingTransaction;
    private AccountInfoEntity outAccount;
    private AccountInfoEntity inAccount;
    private CreateTransactionRequest createTransactionRequest;

    @BeforeEach
    void setUp() {
//        MockitoAnnotations.openMocks(this);
//
//        // 初始化有效请求
//        validRequest = mock(UpdateTransactionRequest.class);
//        when(validRequest.getAccountInfoId()).thenReturn(1L);
//        when(validRequest.getAmount()).thenReturn(new BigDecimal("100.00"));
//        when(validRequest.getCurrency()).thenReturn("CNY");
//        when(validRequest.getId()).thenReturn(1L);
//        when(validRequest.getInCardNo()).thenReturn("IN123456");
//        when(validRequest.getTransactionType()).thenReturn(1);
//        when(validRequest.getUserId()).thenReturn(100L);
//        when(validRequest.getTransactionNo()).thenReturn("TRANS123");
//
        // 初始化存在的交易
        existingTransaction = TransactionEntity.builder()
                .id(1L)
                .transactionNo("TRANS123")
                .transactionType(1)
                .amount(new BigDecimal("50.00"))
                .currency("CNY")
                .state(0)
                .userId(100L)
                .accountInfoId(1L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .createName("test")
                .updateName("test")
                .build();

        // 初始化转出账户
        outAccount = new AccountInfoEntity();
        outAccount.setId(1L);
        outAccount.setBalance(new BigDecimal("200.00"));
        outAccount.setCardNo("OUT123456");
        outAccount.setCurrency("CNY");
        outAccount.setState(1);

        // 初始化转入账户
        inAccount = new AccountInfoEntity();
        inAccount.setId(2L);
        inAccount.setBalance(new BigDecimal("500.00"));
        inAccount.setCardNo("IN123456");
        inAccount.setCurrency("CNY");
        inAccount.setState(1);

        validRequest = new UpdateTransactionRequest();
        validRequest.setAccountInfoId(1L);
        validRequest.setAmount(new BigDecimal("100"));
        validRequest.setCurrency("CNY");
        validRequest.setInCardNo("B12345678910");
        validRequest.setTransactionNo("TXN20241001");
        validRequest.setTransactionType(1);
        validRequest.setUserId(100L);

        createTransactionRequest = new CreateTransactionRequest();
        createTransactionRequest.setAccountInfoId(1L);
        createTransactionRequest.setAmount(new BigDecimal("100"));
        createTransactionRequest.setCurrency("CNY");
        createTransactionRequest.setInCardNo("B12345678910");
        createTransactionRequest.setTransactionNo("TXN20241001");
        createTransactionRequest.setTransactionType(1);
        createTransactionRequest.setUserId(100L);
    }

    /**
     * 更新参数基础校验
     */
    @Test
    void testUpdateTransaction_InvalidParam() {

        validRequest.setTransactionNo("");
        Response<?> response = transactionService.updateTransaction(validRequest);

        assertEquals(TransactionStatusCode.INVALID_PARAM.getCode(), response.getCode());
    }

    /**
     * 交易号不存在
     */
    @Test
    void testUpdateTransaction_TransactionNotFound() {

        Response<?> response = transactionService.updateTransaction(validRequest);
        assertEquals(TransactionStatusCode.TRANSACTION_NOT_FOUND.getCode(), response.getCode());
    }

    /**
     * 检查交易状态是否支持更新
     */
    @Test
    void testUpdateTransaction_TransactionStatusError() {

        // 非待处理状态
        existingTransaction.setState(1);
        when(transactionRepository.getTransaction(anyString()))
                .thenReturn(existingTransaction);

        Response<?> response = transactionService.updateTransaction(validRequest);
        //如果返回交易状态错误，测试用例通过，否则测试用例失败
        assertEquals(TransactionStatusCode.TRANSACTION_STATUS_ERROR.getCode(), response.getCode());
    }

    /**
     * 账户不存在
     */
    @Test
    void testUpdateTransaction_AccountNotFoundError() {
        when(transactionRepository.getTransaction(anyString()))
                .thenReturn(existingTransaction);
        //转出账户不存在
        when(accountInfoRepository.getAccountInfo(anyLong()))
                .thenReturn(null);

        Response<?> response = transactionService.updateTransaction(validRequest);
        //如果返回账户状态异常，测试用例通过，否则测试用例失败
        assertEquals(TransactionStatusCode.ACCOUNT_STATUS_ERROR.getCode(), response.getCode());
    }

    /**
     * 交易币种错误
     */
    @Test
    void testUpdateTransaction_CurrencyMismatch() {
        when(transactionRepository.getTransaction(anyString()))
                .thenReturn(existingTransaction);
        when(accountInfoRepository.getAccountInfo(anyLong()))
                .thenReturn(outAccount);
        outAccount.setCurrency("USD");
        Response<?> response = transactionService.updateTransaction(validRequest);

        //如果返回币种错误，测试用例通过，否则测试用例失败
        assertEquals(TransactionStatusCode.INVALID_TRANSACTION_DATA.getCode(), response.getCode());
        assertEquals("当前账户不支持该币种！", response.getMessage());
    }

    @Test
    void testUpdateTransaction_Success() {

        //先新增一条待处理的交易记录
        transactionRepository.newTransaction(existingTransaction);
        //更新交易记录
        UpdateTransactionRequest validRequest = new UpdateTransactionRequest();
        validRequest.setAccountInfoId(1L);
        validRequest.setAmount(new BigDecimal("100"));
        validRequest.setCurrency("CNY");
        validRequest.setInCardNo("B12345678910");
        validRequest.setTransactionNo("TRANS123");
        validRequest.setTransactionType(1);
        validRequest.setUserId(100L);
        Response<?> response = transactionService.updateTransaction(validRequest);

        assertNotNull(response);
        assertEquals(TransactionStatusCode.TRANSACTION_SUCCESS.getCode(), response.getCode());
    }

    /**
     * 未知异常处理
     */
    @Test
    void testUpdateTransaction_SystemException() {
        when(transactionRepository.getTransaction(anyString()))
                .thenThrow(new RuntimeException("Database error"));

        Response<?> response = transactionService.updateTransaction(validRequest);

        assertEquals(TransactionStatusCode.TRANSACTION_UPDATE_FAILED.getCode(), response.getCode());
    }

    /**
     * 测试删除交易
     */
    @Test
    void testDeleteTransaction_Success() {

        //先新增一条待处理的交易记录
        transactionRepository.newTransaction(existingTransaction);
        System.out.println("删除前数据总数" + TransactionRepositoryImpl.TRANSACTION_LIST.size());
        Response<?> response = transactionService.deleteTransaction(existingTransaction.getTransactionNo());
        System.out.println("删除后数据总数" + TransactionRepositoryImpl.TRANSACTION_LIST.size());
        assertEquals(TransactionStatusCode.TRANSACTION_SUCCESS.getCode(), response.getCode());
    }

    /**
     * 测试当前交易状态不允许删除
     */
    @Test
    void testDeleteTransaction_TransactionStatusError() {
        //先新增一条处理中的交易记录
        existingTransaction.setState(1);
        transactionRepository.newTransaction(existingTransaction);
        Response<?> response = transactionService.deleteTransaction(existingTransaction.getTransactionNo());
        //返回交易状态错误，测试用例通过，否则失败
        assertEquals(TransactionStatusCode.TRANSACTION_STATUS_ERROR.getCode(), response.getCode());
    }
}

