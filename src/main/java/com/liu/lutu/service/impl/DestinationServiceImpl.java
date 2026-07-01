package com.liu.lutu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.lutu.domain.po.Destination;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.mapper.DestinationMapper;
import com.liu.lutu.service.IDestinationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 热门目的地表 服务实现类
 * </p>
 *
 * @author liu
 * @since 2026-04-24
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DestinationServiceImpl extends ServiceImpl<DestinationMapper, Destination> implements IDestinationService {

  private final DestinationMapper destinationMapper;
  private final StringRedisTemplate redisTemplate;

  private static final String CACHE_KEY_PREFIX = "dest:";
  private static final long CACHE_TTL = 30;

  @Override
  public Result<List<Destination>> getHotDestinations(Integer limit) {
    String cacheKey = CACHE_KEY_PREFIX + "hot:" + limit;

    // 尝试从缓存获取
    String cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
      log.debug("从缓存获取热门目的地");
      // 简化处理，实际应该序列化/反序列化
    }

    LambdaQueryWrapper<Destination> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Destination::getStatus, 1)
        .orderByAsc(Destination::getSortOrder);

    List<Destination> allList = destinationMapper.selectList(wrapper);

    // 随机打乱顺序
    Collections.shuffle(allList);

    // 截取前 limit 条
    List<Destination> list = allList.stream()
        .limit(limit)
        .collect(Collectors.toList());

    // 缓存结果
    redisTemplate.opsForValue().set(cacheKey, LocalDateTime.now().toString(), CACHE_TTL, TimeUnit.MINUTES);

    return Result.success(list);
  }

  @Override
  public Result<List<Destination>> getDestinationsBySeason(String season) {
    String cacheKey = CACHE_KEY_PREFIX + "season:" + season;

    LambdaQueryWrapper<Destination> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Destination::getStatus, 1)
        .like(Destination::getBestSeason, season)
        .orderByAsc(Destination::getSortOrder);

    List<Destination> list = destinationMapper.selectList(wrapper);
    return Result.success(list);
  }

  @Override
  public void incrementViewCount(Long id) {
    Destination destination = destinationMapper.selectById(id);
    if (destination != null) {
      destination.setViewCount(destination.getViewCount() + 1);
      destinationMapper.updateById(destination);
    }
  }
}
