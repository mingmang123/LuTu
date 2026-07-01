package com.liu.lutu.service;

import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlanFavorite;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 行程收藏 服务类
 */
public interface ITravelPlanFavoriteService extends IService<TravelPlanFavorite> {

    /**
     * 收藏/取消收藏行程
     *
     * @param shareId 分享ID
     * @return 结果
     */
    Result toggleFavorite(Long shareId);

    /**
     * 检查是否已收藏
     *
     * @param shareId 分享ID
     * @return 是否已收藏
     */
    boolean hasFavorited(Long shareId);

    /**
     * 获取我的收藏列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 收藏列表
     */
    Result getMyFavorites(Integer page, Integer size);

    /**
     * 克隆行程（将分享的行程复制为自己的行程）
     *
     * @param shareId 分享ID
     * @return 新行程ID
     */
    Result<Long> clonePlan(Long shareId);
}
