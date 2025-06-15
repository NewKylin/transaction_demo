package org.yanxun.transaction.vo.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author yanxun
 * @since 2025/6/14 21:27
 */
@Data
@Builder
public class PageResponse<T> {

    /**
     * 总页数
     */
    private Long pageNum;

    /**
     * 总记录数
     */
    private Long totalRecord;

    /**
     * 数据
     */
    private List<T> data;

}
