package fan.fancy.starter.mybatis.plus.query;

import lombok.Setter;

/**
 * 分页查询参数.
 *
 * @author Fan
 */
@Setter
public class PageQuery {

    private Integer currentPage;

    private Integer pageSize;

    public int getCurrentPage() {
        return currentPage == null || currentPage < 1 ? 1 : currentPage;
    }

    public int getPageSize() {
        return pageSize == null || pageSize < 1 ? 10 : pageSize;
    }
}
