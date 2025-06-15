package org.yanxun.transaction.vo.common;

/**
 * 分页查询
 * @author yanxun
 * @since 2025/6/14 21:28
 */
public class PageRequest {

    /**
     * 页码
     */
    private Integer pageIndex;

    /**
     * 页大小
     */
    private Integer pageSize;

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }
}
