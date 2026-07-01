package com.liu.lutu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelGuide;

import java.util.List;

/**
 * <p>
 * 旅行攻略表 服务类
 * </p>
 *
 * @author liu
 * @since 2026-04-24
 */
public interface ITravelGuideService extends IService<TravelGuide> {

    /**
     * 获取推荐攻略列表
     * @param limit 数量限制
     * @return 攻略列表
     */
    Result<List<TravelGuide>> getRecommendedGuides(Integer limit);

    /**
     * 根据目的地获取攻略
     * @param destinationId 目的地ID
     * @return 攻略列表
     */
    Result<List<TravelGuide>> getGuidesByDestination(Long destinationId);

    /**
     * 增加浏览次数
     * @param id 攻略ID
     */
    void incrementViewCount(Long id);
}
