package com.liu.lutu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.lutu.domain.po.Destination;
import com.liu.lutu.domain.po.Result;

import java.util.List;

/**
 * <p>
 * 热门目的地表 服务类
 * </p>
 *
 * @author liu
 * @since 2026-04-24
 */
public interface IDestinationService extends IService<Destination> {

    /**
     * 获取热门目的地列表
     * @param limit 数量限制
     * @return 目的地列表
     */
    Result<List<Destination>> getHotDestinations(Integer limit);

    /**
     * 根据季节获取推荐目的地
     * @param season 季节
     * @return 目的地列表
     */
    Result<List<Destination>> getDestinationsBySeason(String season);

    /**
     * 增加浏览次数
     * @param id 目的地ID
     */
    void incrementViewCount(Long id);
}
