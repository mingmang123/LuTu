package com.liu.lutu.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TravelPlanVo implements Serializable {
    /**
     * 旅行ID
     */
    private Long id;

    /**
     * 旅行名称
     */
    private String title;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 总花费
     */
    private BigDecimal totalAmount;
}
