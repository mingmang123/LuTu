package com.liu.lutu.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.liu.lutu.domain.emun.TravelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelPlanItemVo implements Serializable {

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
     * 出发地(交通：出发地，游玩：景点，住宿：酒店)
     */
    private String fromLocation;

    /**
     * 目的地（交通：目的地，游玩：景点，住宿：酒店）
     */
    private String toLocation;

    /**
     * 交通工具(若是交通，则填写交通工具，否则为空)
     */
    private String transportType;
}
