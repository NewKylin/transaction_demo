package org.yanxun.transaction.vo.response;

import lombok.Builder;
import lombok.Data;

/**
 * @author yanxun
 * @since 2025/6/15 8:54
 */
@Builder
@Data
public class Response<T> {

    private String message;

    private T data;

    private Integer code;


    public static <T> Response<T> success(T data) {
        return Response.<T>builder()
                .message("success")
                .data(data)
                .code(1)
                .build();
    }
}
