package org.yanxun.transaction;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yanxun.transaction.exception.TransactionStatusCode;
import org.yanxun.transaction.reponsitory.TransactionRepository;
import org.yanxun.transaction.reponsitory.entity.AccountInfoEntity;
import org.yanxun.transaction.reponsitory.entity.TransactionEntity;
import org.yanxun.transaction.reponsitory.impl.AccountInfoRepositoryImpl;
import org.yanxun.transaction.service.impl.TransactionServiceImpl;
import org.yanxun.transaction.vo.request.CreateTransactionRequest;
import org.yanxun.transaction.vo.response.Response;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CreateTransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Spy
    private AccountInfoRepositoryImpl accountInfoRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private CreateTransactionRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateTransactionRequest();
        validRequest.setAccountInfoId(1L);
        validRequest.setAmount(new BigDecimal("100"));
        validRequest.setCurrency("CNY");
        validRequest.setInCardNo("B12345678910");
        validRequest.setTransactionNo("TXN20241001");
        validRequest.setTransactionType(1);
        validRequest.setUserId(100L);

    }

    /**
     * 测试创建交易成功
     */
    @Test
    void testCreateTransaction_success() {
        // Arrange
        AccountInfoEntity outAccount = new AccountInfoEntity();
        outAccount.setId(1L);
        outAccount.setBalance(new BigDecimal("200"));
        outAccount.setCurrency("CNY");
        outAccount.setState(1);

        AccountInfoEntity inAccount = new AccountInfoEntity();
        inAccount.setId(2L);
        inAccount.setBalance(new BigDecimal("100"));
        inAccount.setCurrency("CNY");
        inAccount.setState(1);

        when(accountInfoRepository.getAccountInfo(1L)).thenReturn(outAccount);
        when(accountInfoRepository.getAccountInfoByCardNo("B12345678910")).thenReturn(inAccount);
        when(transactionRepository.getTransaction("TXN20241001")).thenReturn(null);

        // Act
        var response = (Response<?>) transactionService.createTransaction(validRequest);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionStatusCode.TRANSACTION_SUCCESS.getCode(), response.getCode());

        verify(transactionRepository, times(1)).newTransaction(any(TransactionEntity.class));

    }

    /**
     * 测试创建交易-并发控制
     */
    @Test
    void testCreateTransaction_duplicateByConcurrentMap() {
        // Arrange
        TransactionServiceImpl.EXECUTE_OPS.put("TXN20241001", true);

        // Act
        var response = (Response<?>) transactionService.createTransaction(validRequest);

        // Assert
        assertNotNull(response);
        //返回交易重复状态测试用例通过，否则认为测试失败
        assertEquals(TransactionStatusCode.TRANSACTION_DUPLICATE.getCode(), response.getCode());
    }

    /**
     * 测试交易号是否已经在数据库中存在
     */
    @Test
    void testCreateTransaction_duplicateByDatabase() {
        when(transactionRepository.getTransaction("TXN20241001")).thenReturn(mock(TransactionEntity.class));
        var response = (Response<?>) transactionService.createTransaction(validRequest);

        assertNotNull(response);
        //返回交易重复状态测试用例通过，否则认为测试失败
        assertEquals(TransactionStatusCode.TRANSACTION_DUPLICATE.getCode(), response.getCode());
    }

    /**
     * 参数基础验证，是否必填等
     */
    @Test
    void testCreateTransaction_invalidParam() {
        // Arrange
        validRequest.setTransactionNo(null);
        validRequest.setAccountInfoId(null);

        // Act
        var response = (Response<?>) transactionService.createTransaction(validRequest);

        // Assert
        assertNotNull(response);
        //返回参数不合法测试用例通过，否则认为测试失败
        assertEquals(TransactionStatusCode.INVALID_PARAM.getCode(), response.getCode());
    }

    /**
     * 账户存在性校验
     */
    @Test
    void testCreateTransaction_accountNotFound() {
        // Arrange
        when(accountInfoRepository.getAccountInfo(1L)).thenReturn(null);

        // Act
        var response = (Response<?>) transactionService.createTransaction(validRequest);

        // Assert
        assertNotNull(response);
        //返回账户状态异常测试用例通过，否则认为测试失败
        assertEquals(TransactionStatusCode.ACCOUNT_STATUS_ERROR.getCode(), response.getCode());
    }

    /**
     * 交易账户状态校验
     */
    @Test
    void testCreateTransaction_accountStateError() {
        // Arrange
        AccountInfoEntity account = new AccountInfoEntity();
        account.setId(1L);
        // 状态异常
        account.setState(0);
        when(accountInfoRepository.getAccountInfo(1L)).thenReturn(account);

        var response = (Response<?>) transactionService.createTransaction(validRequest);

        assertNotNull(response);
        //返回账户状态异常测试用例通过，否则认为测试失败
        assertEquals(TransactionStatusCode.ACCOUNT_STATUS_ERROR.getCode(), response.getCode());
    }

    /**
     * 交易币种与当前账户币种是否一致校验
     */
    @Test
    void testCreateTransaction_currencyMismatch() {
        AccountInfoEntity account = new AccountInfoEntity();
        account.setId(1L);
        account.setCurrency("USD");
        account.setState(1);
        when(accountInfoRepository.getAccountInfo(1L)).thenReturn(account);

        var response = (Response<?>) transactionService.createTransaction(validRequest);
        assertNotNull(response);
        //返回交易数据无效测试用例通过，否则认为测试失败
        assertEquals(TransactionStatusCode.INVALID_TRANSACTION_DATA.getCode(), response.getCode());
    }

    /**
     * 转入账户存在性校验，不存在不允许交易
     */
    @Test
    void testCreateTransaction_inAccountNotFound() {
        AccountInfoEntity account = new AccountInfoEntity();
        account.setId(1L);
        account.setCurrency("CNY");
        account.setState(1);
        when(accountInfoRepository.getAccountInfo(1L)).thenReturn(account);
        when(accountInfoRepository.getAccountInfoByCardNo("1234567890")).thenReturn(null);

        var response = (Response<?>) transactionService.createTransaction(validRequest);
        assertNotNull(response);
        //返回账户状态异常测试用例通过，否则认为测试失败
        assertEquals(TransactionStatusCode.ACCOUNT_STATUS_ERROR.getCode(), response.getCode());
    }

    /**
     * 账户余额不足
     */
    @Test
    void testCreateTransaction_balanceNotEnough() {
        AccountInfoEntity account = new AccountInfoEntity();
        account.setId(1L);
        account.setBalance(new BigDecimal("50"));
        account.setCurrency("CNY");
        account.setState(1);
        when(accountInfoRepository.getAccountInfo(1L)).thenReturn(account);
        // 另一个方法走真实逻辑（假设 getAccountInfoByCardNo 未被 stub）
        //when(accountInfoRepository.getAccountInfoByCardNo("B12345678910")).thenCallRealMethod();
        var response = (Response<?>) transactionService.createTransaction(validRequest);

        assertNotNull(response);
        //返回账户余额不足测试用例通过，否则认为测试失败
        assertEquals(TransactionStatusCode.ACCOUNT_NOT_ENOUGH_BALANCE.getCode(), response.getCode());
    }

    /**
     * 系统异常捕获，预料之外的异常
     */
    @Test
    void testCreateTransaction_systemException() {
        // Arrange
        AccountInfoEntity account = new AccountInfoEntity();
        account.setId(1L);
        account.setBalance(new BigDecimal("200"));
        account.setCurrency("CNY");
        account.setState(1);
        when(accountInfoRepository.getAccountInfo(1L)).thenThrow(new RuntimeException("DB Error"));
        var response = (Response<?>) transactionService.createTransaction(validRequest);
        assertNotNull(response);
        //返回交易创建失败测试用例通过，否则认为测试失败
        assertEquals(TransactionStatusCode.TRANSACTION_CREATE_FAILED.getCode(), response.getCode());
    }
}

