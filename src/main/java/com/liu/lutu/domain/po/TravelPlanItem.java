package com.liu.lutu.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.liu.lutu.domain.emun.TravelTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 旅行环节明细表
 * </p>
 *
 * @author liu
 * @since 2026-04-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("travel_plan_item")
public class TravelPlanItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 环节ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属旅行ID
     */
    private Long travelPlanId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 1=交通 2=游玩 3=住宿
     */
    private TravelTypeEnum itemType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 花费
     */
    private BigDecimal amount;

    /**
     * 出发地
     */
    private String fromLocation;

    /**
     * 目的地
     */
    private String toLocation;

    /**
     * 交通工具
     */
    private String transportType;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
