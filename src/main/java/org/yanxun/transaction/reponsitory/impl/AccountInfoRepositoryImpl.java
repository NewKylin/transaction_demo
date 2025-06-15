package org.yanxun.transaction.reponsitory.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yanxun.transaction.reponsitory.AccountInfoRepository;
import org.yanxun.transaction.reponsitory.entity.AccountInfoEntity;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 账户信息仓储实现类
 * @author yanxun
 * @since 2025/6/14 22:22
 */
@Service
public class AccountInfoRepositoryImpl implements AccountInfoRepository {

    //固定几个账户
    private static final List<AccountInfoEntity> ACCOUNT_INFO_LIST = new ArrayList<>();
    private static final String JSON_FILE_PATH = "/data/accounts.json";

    static {
        loadAccountsFromJson();
    }

    @Override
    public AccountInfoEntity getAccountInfo(Long accountId) {
        //根据账户ID查询账户信息
        return ACCOUNT_INFO_LIST.stream().filter(p -> p.getId().equals(accountId)).findFirst().orElse(null);
    }

    @Override
    public AccountInfoEntity getAccountInfoByCardNo(String cardNo) {
        AccountInfoEntity accountInfoEntity = ACCOUNT_INFO_LIST.stream().filter(p -> p.getCardNo().equals(cardNo)).findFirst().orElse(null);
        return accountInfoEntity;
    }

    private static void loadAccountsFromJson() {
        ObjectMapper mapper = new ObjectMapper();

        try {
            ClassPathResource resource = new ClassPathResource("/data/accounts.json");

            try (InputStream inputStream = resource.getInputStream()) {
                List<AccountInfoJson> accounts = mapper.readValue(inputStream, new TypeReference<List<AccountInfoJson>>() {});

                for (AccountInfoJson json : accounts) {
                    AccountInfoEntity entity = new AccountInfoEntity();
                    entity.setId(json.id);
                    entity.setUserId(json.userId);
                    entity.setBalance(new BigDecimal(json.balance));
                    entity.setCurrency(json.currency);
                    entity.setCardNo(json.cardNo);
                    entity.setState(json.state);

                    ACCOUNT_INFO_LIST.add(entity);
                }

                System.out.println("成功加载 " + ACCOUNT_INFO_LIST.size() + " 个账户数据");
            }
        } catch (Exception e) {
            throw new RuntimeException("加载 accounts.json 失败: " + e.getMessage(), e);
        }
    }

    @Data
    private static class AccountInfoJson {
        public Long id;
        public Long userId;
        public String balance;
        public String currency;
        public String cardNo;
        public Integer state;
    }
}
