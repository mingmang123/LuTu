package com.liu.lutu.service;

import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelUserPortrait;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.lutu.domain.vo.TravelUserPortraitVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liu
 * @since 2026-04-18
 */
public interface ITravelUserPortraitService extends IService<TravelUserPortrait> {

    /**
     * 获取用户画像
     * @return
     */
    Result<TravelUserPortraitVo> getByUserId();

    /**
     * 更新用户画像
     * @param travelUserPortrait
     * @return
     */
    Result updateByUserId(TravelUserPortrait travelUserPortrait);

    /**
     * 添加用户画像
     * @param travelUserPortrait
     * @return
     */
    Result add(TravelUserPortrait travelUserPortrait);
}
