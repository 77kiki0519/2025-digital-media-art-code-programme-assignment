package org.example.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    
    private List<T> records;
    private Long total;
    private Long currentPage;
    private Long pageSize;
    private Long totalPages;
    
    public PageResult(List<T> records, Long total, Long currentPage, Long pageSize) {
        this.records = records;
        this.total = total;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = (total + pageSize - 1) / pageSize;
    }
}


