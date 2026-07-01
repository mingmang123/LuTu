package com.liu.lutu.service;

import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlanItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.lutu.domain.vo.TravelPlanItemVo;

import java.util.List;

/**
 * <p>
 * 旅行环节明细表 服务类
 * </p>
 *
 * @author liu
 * @since 2026-04-03
 */
public interface ITravelPlanItemService extends IService<TravelPlanItem> {

    /**
     * 根据行程id获取行程明细
     * @param tripId
     * @return
     */
    Result<List<TravelPlanItemVo>> getByTripId(Long tripId);

    /**
     * 批量添加出行环节
     * @param travelItem
     * @return
     */
    Result adds(List<TravelPlanItem> travelItem);

    /**
     * 批量更新出行环节
     * @param travelItem
     * @return
     */
    Result updateByIds(List<TravelPlanItem> travelItem);

    /**
     * 批量删除出行环节
     * @param travelItem
     * @return
     */
    Result delete(List<TravelPlanItem> travelItem);
}
