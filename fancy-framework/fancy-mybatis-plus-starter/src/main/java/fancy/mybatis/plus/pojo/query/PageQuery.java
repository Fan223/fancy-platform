package fancy.mybatis.plus.pojo.query;

/**
 * 分页查询参数.
 *
 * @author Fan
 * @since 2025/3/6 9:45
 */
public class PageQuery {

    private Long currentPage;

    private Long pageSize;

    public long getCurrentPage() {
        if (null == currentPage || currentPage <= 0L) {
            currentPage = 1L;
        }
        return currentPage;
    }

    public void setCurrentPage(Long currentPage) {
        this.currentPage = currentPage;
    }

    public long getPageSize() {
        if (null == pageSize || pageSize <= 0L) {
            pageSize = 12L;
        }
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
