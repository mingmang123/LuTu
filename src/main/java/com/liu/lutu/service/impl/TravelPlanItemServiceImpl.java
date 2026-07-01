package com.liu.lutu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.lutu.domain.emun.TravelTypeEnum;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlanItem;
import com.liu.lutu.domain.vo.TravelPlanItemVo;
import com.liu.lutu.mapper.TravelPlanItemMapper;
import com.liu.lutu.service.ITravelPlanItemService;
import com.liu.lutu.util.ThreadlocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 旅行环节明细表 服务实现类
 * </p>
 *
 * @author liu
 * @since 2026-04-03
 */
@Slf4j
@Service
public class TravelPlanItemServiceImpl extends ServiceImpl<TravelPlanItemMapper, TravelPlanItem> implements ITravelPlanItemService {

    /**
     * 根据行程id获取行程明细
     * @param tripId
     * @return
     */
    @Override
    public Result<List<TravelPlanItemVo>> getByTripId(Long tripId) {
        Long userId = ThreadlocalUtil.getUserId();
        log.info("TravelPlanItemServiceImpl.getByTripId 被调用，tripId={}, ThreadLocal中的userId={}", tripId, userId);

        if(userId == null){
            log.error("用户未登录，ThreadLocal中无用户ID");
            return Result.fail("用户未登录");
        }
        // 查询用户的出行计划
        List<TravelPlanItem> list = lambdaQuery()
                .eq(TravelPlanItem::getTravelPlanId, tripId)
                .eq(TravelPlanItem::getUserId, userId)
                .orderByAsc(TravelPlanItem::getEndTime)
                .list();
        log.info("查询到 {} 条行程环节记录", list.size());

        //如果没有数据，则返回空列表
        if(CollectionUtils.isEmpty(list)){
            return Result.success(Collections.emptyList());
        }
        //转换为vo
        List<TravelPlanItemVo> voList = new ArrayList<>();
        for (TravelPlanItem planItem : list) {
            TravelPlanItemVo vo = new TravelPlanItemVo();
            BeanUtils.copyProperties(planItem, vo);
            //如果是景点或者酒店，把transportType设置为空
            if(planItem.getItemType().equals(TravelTypeEnum.PLAY.getCode())||planItem.getItemType().equals(TravelTypeEnum.HOUSE.getCode())){
                vo.setTransportType("null");
            }
            voList.add(vo);
        }

        return Result.success(voList);
    }

    /**
     * 批量添加出行环节
     * @param travelItem
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result adds(List<TravelPlanItem> travelItem) {


        if(CollectionUtils.isEmpty(travelItem)){
            return Result.fail("添加的出行计划为空");
        }
        Long userId = ThreadlocalUtil.getUserId();
        if(userId == null){
            return Result.fail("用户未登录");
        }
        //设置用户ID
        for (TravelPlanItem planItem : travelItem) {
            planItem.setUserId(userId);
        }
        //拿出同一个旅行计划的所有出行计划
        List<TravelPlanItem> existItems = lambdaQuery()
                .eq(TravelPlanItem::getTravelPlanId, travelItem.get(0).getTravelPlanId())
                .eq(TravelPlanItem::getUserId, userId)
                .list();



        //判断是否与现有环节时间重叠
        if(isOverlap(travelItem, existItems)){
            return Result.fail("时间重叠，请重新选择时间");
        }

        saveBatch(travelItem);

        return Result.success();
    }

    /**
     * 批量更新出行环节
     * @param travelItem
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateByIds(List<TravelPlanItem> travelItem) {
        if(CollectionUtils.isEmpty(travelItem)){
            return Result.fail("更新的出行计划为空");
        }
        Long userId = ThreadlocalUtil.getUserId();
        if(userId == null){
            return Result.fail("用户未登录");
        }
        //设置用户ID
        for (TravelPlanItem planItem : travelItem) {
            planItem.setUserId(userId);
        }
        //拿出同一个旅行计划的所有出行计划
        List<TravelPlanItem> existItems = lambdaQuery()
                .eq(TravelPlanItem::getTravelPlanId, travelItem.get(0).getTravelPlanId())
                .eq(TravelPlanItem::getUserId, userId)
                .list();
        //判断是否与现有环节时间重叠
        if(isOverlap(travelItem, existItems)){
            return Result.fail("时间重叠，请重新选择时间");
        }
        updateBatchById(travelItem);
        return Result.success();

    }

    /**
     * 批量删除出行环节
     * @param travelItem
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result delete(List<TravelPlanItem> travelItem) {
        if(CollectionUtils.isEmpty(travelItem)){
            return Result.fail("删除的出行计划为空");
        }
        Long userId = ThreadlocalUtil.getUserId();
        if(userId == null){
            return Result.fail("用户未登录");
        }
        //设置用户ID
        for (TravelPlanItem planItem : travelItem) {
            planItem.setUserId(userId);
        }
        removeBatchByIds(travelItem);
        return Result.success();
    }

    //统计一个旅行计划的所有环节的花费
    public BigDecimal getTotalCost(Long tripId) {
        //获取用户ID
        Long userId = ThreadlocalUtil.getUserId();
        BigDecimal reduce = lambdaQuery().eq(TravelPlanItem::getTravelPlanId, tripId)
                .eq(TravelPlanItem::getUserId, userId)
                .list()
                .stream()
                .map(TravelPlanItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return reduce;

    }




    // 判断是否与现有环节时间重叠
    private boolean isOverlap(List<TravelPlanItem> newItems, List<TravelPlanItem> existItems) {
        for (TravelPlanItem newItem : newItems) {
            LocalDateTime newStart = newItem.getStartTime();
            LocalDateTime newEnd = newItem.getEndTime();

            for (TravelPlanItem exist : existItems) {
                // 如果是编辑自己，跳过自己这条
                if (newItem.getId() != null && newItem.getId().equals(exist.getId())) {
                    continue;
                }

                LocalDateTime eStart = exist.getStartTime();
                LocalDateTime eEnd = exist.getEndTime();

                // 重叠判断公式
                if (newStart.isBefore(eEnd) && eStart.isBefore(newEnd)) {
                    return true;
                }
            }
        }
        return false;
    }
}
