package com.liu.lutu.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelGuide;
import com.liu.lutu.mapper.TravelGuideMapper;
import com.liu.lutu.service.ITravelGuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 旅行攻略表 服务实现类
 * </p>
 *
 * @author liu
 * @since 2026-04-24
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TravelGuideServiceImpl extends ServiceImpl<TravelGuideMapper, TravelGuide> implements ITravelGuideService {

    private final TravelGuideMapper travelGuideMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String CACHE_KEY_PREFIX = "guide:";
    private static final long CACHE_TTL = 30;

    @Override
    public Result<List<TravelGuide>> getRecommendedGuides(Integer limit) {
        String cacheKey = CACHE_KEY_PREFIX + "rec:" + limit;

        // 尝试从缓存获取
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("从缓存获取推荐攻略");
            List<TravelGuide> cachedList = JSONUtil.toList(cached, TravelGuide.class);
            return Result.success(cachedList);
        }

        LambdaQueryWrapper<TravelGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelGuide::getStatus, 1)
                .orderByAsc(TravelGuide::getSortOrder)
                .last("LIMIT " + limit);

        List<TravelGuide> list = travelGuideMapper.selectList(wrapper);

        // 缓存结果（序列化为 JSON）
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(list), CACHE_TTL, TimeUnit.MINUTES);

        return Result.success(list);
    }

    @Override
    public Result<List<TravelGuide>> getGuidesByDestination(Long destinationId) {
        LambdaQueryWrapper<TravelGuide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelGuide::getStatus, 1)
                .eq(TravelGuide::getDestinationId, destinationId)
                .orderByDesc(TravelGuide::getCreateTime);
        
        List<TravelGuide> list = travelGuideMapper.selectList(wrapper);
        return Result.success(list);
    }

    @Override
    public void incrementViewCount(Long id) {
        TravelGuide guide = travelGuideMapper.selectById(id);
        if (guide != null) {
            guide.setViewCount(guide.getViewCount() + 1);
            travelGuideMapper.updateById(guide);
        }
    }
}
