package com.liu.lutu.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelUserPortrait;
import com.liu.lutu.domain.vo.TravelUserPortraitVo;
import com.liu.lutu.mapper.TravelUserPortraitMapper;
import com.liu.lutu.service.ITravelUserPortraitService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.lutu.util.ThreadlocalUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liu
 * @since 2026-04-18
 */
@Service
public class TravelUserPortraitServiceImpl extends ServiceImpl<TravelUserPortraitMapper, TravelUserPortrait> implements ITravelUserPortraitService {

    /**
     * 获取用户画像
     * @return
     */
    @Override
    public Result<TravelUserPortraitVo> getByUserId() {
        Long userId = ThreadlocalUtil.getUserId();
        // 查询用户画像
        TravelUserPortrait travelUserPortrait = lambdaQuery().eq(TravelUserPortrait::getUserId, userId).one();
        // 转换为vo
        TravelUserPortraitVo travelUserPortraitVo = new TravelUserPortraitVo();
        BeanUtils.copyProperties(travelUserPortrait, travelUserPortraitVo);
        return Result.success(travelUserPortraitVo);
    }

    /*
     * 更新用户画像
     */
    @Override
    public Result updateByUserId(TravelUserPortrait travelUserPortrait) {
        Long userId = ThreadlocalUtil.getUserId();
        travelUserPortrait.setUserId(userId);
        travelUserPortrait.setUpdateTime(LocalDateTime.now());
        lambdaUpdate().eq(TravelUserPortrait::getUserId, userId).update(travelUserPortrait);
        return Result.success();
    }

    /*
     * 添加用户画像
     */
    @Override
    public Result add(TravelUserPortrait travelUserPortrait) {
        Long userId = ThreadlocalUtil.getUserId();
        travelUserPortrait.setUserId(userId);
        travelUserPortrait.setCreatTime(LocalDateTime.now());
        travelUserPortrait.setUpdateTime(LocalDateTime.now());
        save(travelUserPortrait);
        return Result.success();
    }
}
