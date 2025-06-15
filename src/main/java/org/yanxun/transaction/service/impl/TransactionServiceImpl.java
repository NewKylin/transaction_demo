package org.yanxun.transaction.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.stereotype.Service;
import org.yanxun.transaction.exception.TransactionStatusCode;
import org.yanxun.transaction.exception.TransactionException;
import org.yanxun.transaction.reponsitory.AccountInfoRepository;
import org.yanxun.transaction.reponsitory.TransactionRepository;
import org.yanxun.transaction.reponsitory.entity.AccountInfoEntity;
import org.yanxun.transaction.reponsitory.entity.FundFlowEntity;
import org.yanxun.transaction.reponsitory.entity.TransactionEntity;
import org.yanxun.transaction.service.TransactionService;
import org.yanxun.transaction.vo.common.PageResponse;
import org.yanxun.transaction.vo.request.CreateTransactionRequest;
import org.yanxun.transaction.vo.request.TransactionPageRequest;
import org.yanxun.transaction.vo.request.UpdateTransactionRequest;
import org.yanxun.transaction.vo.response.FundFlowResponse;
import org.yanxun.transaction.vo.response.Response;
import org.yanxun.transaction.vo.response.TransactionPageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * @author yanxun
 * @since 2025/6/14 21:59
 */
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountInfoRepository accountInfoRepository;

    public static final ConcurrentHashMap<String, Boolean> EXECUTE_OPS = new ConcurrentHashMap<>();

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountInfoRepository accountInfoRepository) {
        this.transactionRepository = transactionRepository;
        this.accountInfoRepository = accountInfoRepository;
    }

    @Override
    public Response<?> createTransaction(CreateTransactionRequest request) {

        try {
            //参数基础验证
            request.checkParam();
            //防重校验
            if(EXECUTE_OPS.putIfAbsent(request.getTransactionNo(), true) != null){
                throw new TransactionException(TransactionStatusCode.TRANSACTION_DUPLICATE);
            }

            if (transactionRepository.getTransaction(request.getTransactionNo()) != null) {
                throw new TransactionException(TransactionStatusCode.TRANSACTION_DUPLICATE);
            }

            //转出账户
            AccountInfoEntity accountInfo = accountInfoRepository.getAccountInfo(request.getAccountInfoId());
            //校验账户是否存在
            if (accountInfo == null || !accountInfo.getState().equals(1)) {
                throw new TransactionException(TransactionStatusCode.ACCOUNT_STATUS_ERROR);
            }
            if (!StringUtils.equals(request.getCurrency(), accountInfo.getCurrency())) {
                throw new TransactionException(TransactionStatusCode.INVALID_TRANSACTION_DATA, "当前账户不支持该币种！");
            }
            //转入账户
            AccountInfoEntity inAccountInfo = accountInfoRepository.getAccountInfoByCardNo(request.getInCardNo());
            //转入账户是否存在
            if (inAccountInfo == null || !inAccountInfo.getState().equals(1)) {
                throw new TransactionException(TransactionStatusCode.ACCOUNT_STATUS_ERROR);
            }

            if(!accountInfo.getUserId().equals(request.getUserId())){
                throw new TransactionException(TransactionStatusCode.TRANSACTION_CREATE_FAILED, "当前账户不属于该用户！");
            }

            //需要给转入和转出账户都上锁，保证线程安全
            synchronized (accountInfo) {
                synchronized (inAccountInfo) {
                    if (request.getTransactionType().equals(1) && accountInfo.getBalance().compareTo(request.getAmount()) < 0) {
                        throw new TransactionException(TransactionStatusCode.ACCOUNT_NOT_ENOUGH_BALANCE);
                    }
                    //注意：以下需要保持事务，因为本次设计不涉及数据持久化，所以省略数据库事务。如果做持久化，通过事务的控制可以保证原子性和一致性
                    //生成一条转出流水
                    FundFlowEntity outFundFlow = FundFlowEntity.builder()
                            .currency(request.getCurrency())
                            .cardNo(accountInfo.getCardNo())
                            .accountInfoId(accountInfo.getId())
                            .balanceBefore(accountInfo.getBalance())
                            .balanceAfter(accountInfo.getBalance().subtract(request.getAmount()))
                            .directionType(1)
                            .amount(request.getAmount())
                            .currency(request.getCurrency())
                            .build();

                    //生成一条转入流水
                    FundFlowEntity inFundFlow = FundFlowEntity.builder()
                            .currency(request.getCurrency())
                            .cardNo(inAccountInfo.getCardNo())
                            .accountInfoId(accountInfo.getId())
                            .balanceBefore(inAccountInfo.getBalance())
                            .balanceAfter(inAccountInfo.getBalance().add(request.getAmount()))
                            .directionType(2)
                            .amount(request.getAmount())
                            .currency(request.getCurrency())
                            .build();

                    List<FundFlowEntity> fundFlowList = List.of(outFundFlow, inFundFlow);
                    transactionRepository.newTransaction(TransactionEntity.builder()
                            .transactionNo(request.getTransactionNo())
                            .transactionType(request.getTransactionType())
                            .amount(request.getAmount())
                            .currency(request.getCurrency())
                            .userId(request.getUserId())
                            .accountInfoId(request.getAccountInfoId())
                            //前端自定义交易传参，只是为了方便测试状态，实际不会这样做
                            .state(request.getState() == null ? 2 : request.getState())
                            .createTime(LocalDateTime.now())
                            .fundFlowList(fundFlowList)
                            .build());

                    accountInfo.setBalance(accountInfo.getBalance().subtract(request.getAmount()));
                    inAccountInfo.setBalance(inAccountInfo.getBalance().add(request.getAmount()));
                }
            }
            return Response.builder()
                    .code(TransactionStatusCode.TRANSACTION_SUCCESS.getCode())
                    .message(TransactionStatusCode.TRANSACTION_SUCCESS.getMessage())
                    .build();
        } catch (TransactionException e) {
            //自定义异常直接通过接口返回状态和错误信息
            return Response.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getErrorCode().getMessage())
                    .build();
        } catch (Exception e){
            //系统异常统一返回交易失败，并且打印日志，注意屏蔽敏感信息。
            log.error("转账交易失败，异常信息：",e);
            return Response.builder()
                    .code(TransactionStatusCode.TRANSACTION_CREATE_FAILED.getCode())
                    .message(TransactionStatusCode.TRANSACTION_CREATE_FAILED.getMessage())
                    .build();
        }finally {
            EXECUTE_OPS.remove(request.getTransactionNo());
        }
    }

    public Response<?> updateTransaction(UpdateTransactionRequest request) {
        try {
            //参数基础验证
            request.checkParam();

            TransactionEntity transaction = transactionRepository.getTransaction(request.getTransactionNo());
            if (transaction == null) {
                throw new TransactionException(TransactionStatusCode.TRANSACTION_NOT_FOUND);
            }
            //只有待处理的交易才能修改
//            if (!transaction.getState().equals(0)) {
//                throw new TransactionException(TransactionStatusCode.TRANSACTION_STATUS_ERROR);
//            }

            //转出账户
            AccountInfoEntity accountInfo = accountInfoRepository.getAccountInfo(request.getAccountInfoId());
            //校验账户是否存在
            if (accountInfo == null || !accountInfo.getState().equals(1)) {
                throw new TransactionException(TransactionStatusCode.ACCOUNT_STATUS_ERROR);
            }
            if (!StringUtils.equals(request.getCurrency(), accountInfo.getCurrency())) {
                throw new TransactionException(TransactionStatusCode.INVALID_TRANSACTION_DATA, "当前账户不支持该币种！");
            }
            //转入账户
            AccountInfoEntity inAccountInfo = accountInfoRepository.getAccountInfoByCardNo(request.getInCardNo());
            //转入账户是否存在
            if (inAccountInfo == null || !inAccountInfo.getState().equals(1)) {
                throw new TransactionException(TransactionStatusCode.ACCOUNT_STATUS_ERROR);
            }

            synchronized (transaction){
                //需要给转入和转出账户都上锁，保证线程安全
                synchronized (accountInfo) {
                    synchronized (inAccountInfo) {
                        //将原交易的金额反向交易回账户，转出金额加回原交易金额
                        accountInfo.setBalance(accountInfo.getBalance().add(transaction.getAmount()));
                        //转入金额减去原交易金额
                        inAccountInfo.setBalance(accountInfo.getBalance().subtract(transaction.getAmount()));

                        //修改交易单
                        transaction.setTransactionType(request.getTransactionType());
                        transaction.setAmount(request.getAmount());
                        transaction.setCurrency(request.getCurrency());
                        transaction.setUserId(request.getUserId());
                        transaction.setAccountInfoId(request.getAccountInfoId());
                        transaction.setUpdateTime(LocalDateTime.now());

                        //生成一条转出流水
                        FundFlowEntity outFundFlow = FundFlowEntity.builder()
                                .currency(request.getCurrency())
                                .cardNo(accountInfo.getCardNo())
                                .accountInfoId(accountInfo.getId())
                                .balanceBefore(accountInfo.getBalance())
                                .balanceAfter(accountInfo.getBalance().subtract(request.getAmount()))
                                .directionType(1)
                                .amount(request.getAmount())
                                .currency(request.getCurrency())
                                .build();

                        //生成一条转入流水
                        FundFlowEntity inFundFlow = FundFlowEntity.builder()
                                .currency(request.getCurrency())
                                .cardNo(inAccountInfo.getCardNo())
                                .accountInfoId(accountInfo.getId())
                                .balanceBefore(inAccountInfo.getBalance())
                                .balanceAfter(inAccountInfo.getBalance().add(request.getAmount()))
                                .directionType(2)
                                .amount(request.getAmount())
                                .currency(request.getCurrency())
                                .build();
                        //更新交易流水
                        transaction.setFundFlowList(List.of(outFundFlow, inFundFlow));

                        //用最新的交易单更新账户信息
                        accountInfo.setBalance(accountInfo.getBalance().subtract(request.getAmount()));
                        inAccountInfo.setBalance(inAccountInfo.getBalance().add(request.getAmount()));
                    }
                }
            }
            return Response.builder()
                    .code(TransactionStatusCode.TRANSACTION_SUCCESS.getCode())
                    .message(TransactionStatusCode.TRANSACTION_SUCCESS.getMessage())
                    .build();
        } catch (TransactionException e) {
            //自定义异常直接通过接口返回状态和错误信息
            return Response.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e){
            //系统异常统一返回交易失败，并且打印日志，注意屏蔽敏感信息。
            log.error("更新交易失败，异常信息：",e);
            return Response.builder()
                    .code(TransactionStatusCode.TRANSACTION_UPDATE_FAILED.getCode())
                    .message(TransactionStatusCode.TRANSACTION_UPDATE_FAILED.getMessage())
                    .build();
        }
    }

    public Response<?> deleteTransaction(String transactionNo) {
        try {

            if(StringUtils.isBlank(transactionNo)){
                throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"交易号不能为空！");
            }

            TransactionEntity transaction = transactionRepository.getTransaction(transactionNo);
            if(transaction == null){
                throw new TransactionException(TransactionStatusCode.TRANSACTION_NOT_FOUND);
            }
            if(!transaction.getState().equals(0)){
                throw new TransactionException(TransactionStatusCode.TRANSACTION_STATUS_ERROR,"交易已经处理或正在处理，不能删除！");
            }
            transactionRepository.deleteTransaction(transactionNo);
            return Response.builder()
                    .code(TransactionStatusCode.TRANSACTION_SUCCESS.getCode())
                    .message(TransactionStatusCode.TRANSACTION_SUCCESS.getMessage())
                    .build();
        } catch (TransactionException e) {
            //自定义异常直接通过接口返回状态和错误信息
            return Response.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } catch (Exception e){
            //系统异常统一返回交易失败，并且打印日志，注意屏蔽敏感信息。
            log.error("删除交易失败，异常信息：",e);
            return Response.builder()
                    .code(TransactionStatusCode.TRANSACTION_UPDATE_FAILED.getCode())
                    .message(TransactionStatusCode.TRANSACTION_UPDATE_FAILED.getMessage())
                    .build();

        }
    }

    @Override
    public Response<PageResponse<TransactionPageResponse>> pageTransaction(TransactionPageRequest request) {

        if(request.getPageIndex() == null || request.getPageIndex() <= 0){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"页码不能为空或者小于0");
        }
        if(request.getPageSize() == null || request.getPageSize() <= 0){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"页大小不能为空或者小于0");
        }
        //TODO:这里应该还要校验传参的用户是否当前登录人，否则会有数据横向越权的风险。但是因为没有包含登录部分的内容，所以这里先不处理
        if(request.getUserId() == null){
            throw new TransactionException(TransactionStatusCode.INVALID_PARAM,"用户ID不能为空");
        }

        Long totalRecord = transactionRepository.countTransaction(request);
        if(totalRecord <= 0){
            return Response.success(null);
        }

        List<TransactionEntity> transactionEntityList = transactionRepository.queryTransaction(request);

        return Response.success(PageResponse.<TransactionPageResponse>builder()
                .pageNum(totalRecord / request.getPageSize() + 1)
                .totalRecord(totalRecord)
                .data(transactionEntityList.stream().map(this::convertTransactionEntityToTransactionPageResponse).toList())
                .build());
    }

    /**
     * 转换TransactionEntity为TransactionPageResponse
     * @param transactionEntity
     * @return 接口列表数据
     */
    private TransactionPageResponse convertTransactionEntityToTransactionPageResponse(TransactionEntity transactionEntity) {
        TransactionPageResponse transactionPageResponse = TransactionPageResponse.builder()
                .transactionNo(transactionEntity.getTransactionNo())
                .transactionType(transactionEntity.getTransactionType())
                .amount(transactionEntity.getAmount())
                .currency(transactionEntity.getCurrency())
                .userId(transactionEntity.getUserId())
                .accountInfoId(transactionEntity.getAccountInfoId())
                .createTime(transactionEntity.getCreateTime())
                .createName(transactionEntity.getCreateName())
                .updateTime(transactionEntity.getUpdateTime())
                .build();

        if(transactionEntity.getFundFlowList() != null && !transactionEntity.getFundFlowList().isEmpty()){
            transactionPageResponse.setFundFlowList(transactionEntity.getFundFlowList().stream()
                    .map(fundFlowEntity -> FundFlowResponse.builder()
                            .id(fundFlowEntity.getId())
                            .accountInfoId(fundFlowEntity.getAccountInfoId())
                            .amount(fundFlowEntity.getAmount())
                            .currency(fundFlowEntity.getCurrency())
                            .balanceBefore(fundFlowEntity.getBalanceBefore())
                            .balanceAfter(fundFlowEntity.getBalanceAfter())
                            .cardNo(fundFlowEntity.getCardNo())
                            .directionType(fundFlowEntity.getDirectionType())
                            .build()
                    ).toList()
            );
        }
        return transactionPageResponse;
    }

    @Override
    public Response<AccountInfoEntity> getAccountInfo(Long accountId) {
        return Response.success(
                accountInfoRepository.getAccountInfo(accountId)
        );
    }
}
