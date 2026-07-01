package com.liu.lutu.service;

import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.lutu.domain.vo.TravelPlanVo;

import java.util.List;

/**
 * <p>
 * 旅行计划表 服务类
 * </p>
 *
 * @author liu
 * @since 2026-04-03
 */
public interface ITravelPlanService extends IService<TravelPlan> {

    /**
     * 获取用户的旅行计划
     * @return
     */
    Result<List<TravelPlanVo>> getByUserId();

    /**
     * 保存用户的旅行计划
     * @param travelPlan
     * @return
     */
    Result saveByUserId(TravelPlan travelPlan);

    /**
     * 删除用户的旅行计划
     * @param id 旅行计划ID
     * @return
     */
    Result removeByUserId(Long id);
}
