package com.liu.lutu.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liu.lutu.domain.emun.TravelTypeEnum;
import com.liu.lutu.domain.po.AiChatSession;
import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlanItem;
import com.liu.lutu.domain.vo.TravelPlanItemVo;
import com.liu.lutu.mapper.AiChatSessionMapper;
import com.liu.lutu.mapper.TravelPlanItemMapper;
import com.liu.lutu.service.ITravelPlanItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TravePlanTool {
  private final TravelPlanItemMapper travelPlanItemMapper;
  private final AiChatSessionMapper aiChatSessionMapper;
  private final ITravelPlanItemService travelPlanItemService;

  /**
   * ai根据对话id获取行程环节明细
   * 
   * @param id 对话id
   * @return 行程环节列表
   */
  @Tool(description = "查询用户的行程环节列表。当用户询问行程、计划、安排、有什么活动等问题时，必须调用此工具获取真实的行程数据。不要凭空回答，必须先查询。")
  public Result<List<TravelPlanItemVo>> getByTripId(
      @ToolParam(description = "当前会话ID且为Long型，用于获取关联的行程数据", required = true) Long id) {
    log.info("========== getByTripId 工具被调用 ==========");
    log.info("传入参数 sessionId: {}", id);

    // 参数校验
    if (id == null) {
      log.error("会话ID不能为空");
      return Result.fail("会话ID不能为空，请提供当前会话ID");
    }

    // 根据会话id获取会话信息
    AiChatSession session = aiChatSessionMapper.selectById(id);
    log.info("查询到的 session: {}", session);

    if (session == null) {
      log.error("会话不存在，sessionId: {}", id);
      return Result.fail("会话不存在");
    }

    // 获取关联的旅行计划id
    Long tripId = session.getTravelPlanId();
    log.info("从 session 获取到的 tripId: {}", tripId);

    // 根据旅行计划id获取行程环节
    log.info("准备调用 travelPlanItemService.getByTripId，tripId: {}", tripId);
    List<TravelPlanItem> list = travelPlanItemMapper.selectList(new QueryWrapper<TravelPlanItem>()
        .eq("travel_plan_id", tripId)
        .eq("user_id", session.getUserId())
        .orderByAsc("start_time")

    );
    // 如果没有数据，则返回空列表
    if (CollectionUtils.isEmpty(list)) {
      return Result.success(Collections.emptyList());
    }

    // 转换为vo
    List<TravelPlanItemVo> voList = new ArrayList<>();
    for (TravelPlanItem planItem : list) {
      TravelPlanItemVo vo = new TravelPlanItemVo();
      BeanUtils.copyProperties(planItem, vo);
      // 如果是景点或者酒店，把transportType设置为空
      if (planItem.getItemType().equals(TravelTypeEnum.PLAY.getCode())
          || planItem.getItemType().equals(TravelTypeEnum.HOUSE.getCode())) {
        vo.setTransportType("null");
      }
      voList.add(vo);
    }

    return Result.success(voList);

  }

  /**
   * ai批量新增行程环节明细
   * 
   * @param travelItems 行程环节列表，JSON数组格式
   * @return 操作结果
   */
  @Tool(description = """
      批量新增行程环节。travelPlanId和userId由后端自动填充。
      参数示例：[{"itemType":"PLAY","startTime":"2026-04-24T09:00:00","endTime":"2026-04-24T12:00:00","fromLocation":"酒店","toLocation":"天安门","amount":100}]
      字段说明：
      - itemType: 类型，必填。PLAY=游玩, FOOD=美食, HOUSE=住宿, TRANSPORT=交通
      - startTime: 开始时间，必填，格式：2026-04-24T09:00:00
      - endTime: 结束时间，必填，格式：2026-04-24T12:00:00
      - fromLocation: 出发地/起点，可选
      - toLocation: 目的地/终点，可选
      - transportType: 交通工具，TRANSPORT类型时必填
      - amount: 花费金额，必填，如果用户未提供则估算一个合理金额
      """)
  public Result add(
      @ToolParam(description = "行程环节列表，JSON数组格式。必填：itemType/startTime/endTime/amount。时间格式：2026-04-24T09:00:00") List<Map<String, Object>> travelItems) {
    if (CollectionUtils.isEmpty(travelItems)) {
      return Result.fail("行程环节列表不能为空");
    }

    // 从ThreadLocal获取会话ID
    Long sessionId = com.liu.lutu.util.ThreadlocalUtil.getSessionId();
    if (sessionId == null) {
      return Result.fail("会话ID不存在，请重新发起对话");
    }

    // 根据会话ID获取旅行计划ID
    AiChatSession session = aiChatSessionMapper.selectById(sessionId);
    if (session == null) {
      return Result.fail("会话不存在");
    }
    Long travelPlanId = session.getTravelPlanId();
    if (travelPlanId == null) {
      return Result.fail("该会话未关联旅行计划");
    }

    // 将Map列表转换为TravelPlanItem列表
    List<TravelPlanItem> items = travelItems.stream()
        .map(map -> convertMapToTravelPlanItem(map, travelPlanId))
        .toList();

    return travelPlanItemService.adds(items);
  }

  /**
   * 将Map转换为TravelPlanItem
   */
  private TravelPlanItem convertMapToTravelPlanItem(Map<String, Object> map, Long travelPlanId) {
    TravelPlanItem item = new TravelPlanItem();
    item.setTravelPlanId(travelPlanId);

    // 设置itemType
    String itemTypeStr = (String) map.get("itemType");
    if (itemTypeStr != null) {
      item.setItemType(TravelTypeEnum.valueOf(itemTypeStr));
    }

    // 设置时间
    String startTimeStr = (String) map.get("startTime");
    if (startTimeStr != null) {
      item.setStartTime(LocalDateTime.parse(startTimeStr));
    }

    String endTimeStr = (String) map.get("endTime");
    if (endTimeStr != null) {
      item.setEndTime(LocalDateTime.parse(endTimeStr));
    }

    // 设置地点
    item.setFromLocation((String) map.get("fromLocation"));
    item.setToLocation((String) map.get("toLocation"));

    // 设置交通工具
    item.setTransportType((String) map.get("transportType"));

    // 设置金额
    Object amountObj = map.get("amount");
    if (amountObj != null) {
      if (amountObj instanceof Number) {
        item.setAmount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
      } else if (amountObj instanceof String) {
        item.setAmount(new BigDecimal((String) amountObj));
      }
    }

    return item;
  }

  /**
   * ai批量更新行程环节明细
   * 
   * @param travelItems 行程环节列表，必须包含id字段（环节ID）
   * @return 操作结果
   */
  @Tool(description = """
      批量更新行程环节。travelPlanId和userId由后端自动填充。
      参数示例：[{"id":1,"itemType":"PLAY","startTime":"2026-04-24T10:00:00","endTime":"2026-04-24T14:00:00","amount":200}]
      字段说明：
      - id: 环节ID（从getByTripId查询结果中获取的id字段），必填
      - 其他字段与新增相同
      """)
  public Result updateByIds(
      @ToolParam(description = "行程环节明细列表，JSON数组格式，每个元素必须包含id字段（环节ID）") List<Map<String, Object>> travelItems) {
    if (CollectionUtils.isEmpty(travelItems)) {
      return Result.fail("行程环节列表不能为空");
    }

    // 从ThreadLocal获取会话ID
    Long sessionId = com.liu.lutu.util.ThreadlocalUtil.getSessionId();
    if (sessionId == null) {
      return Result.fail("会话ID不存在，请重新发起对话");
    }

    // 根据会话ID获取旅行计划ID
    AiChatSession session = aiChatSessionMapper.selectById(sessionId);
    if (session == null) {
      return Result.fail("会话不存在");
    }
    Long travelPlanId = session.getTravelPlanId();
    if (travelPlanId == null) {
      return Result.fail("该会话未关联旅行计划");
    }

    // 将Map列表转换为TravelPlanItem列表
    List<TravelPlanItem> items = travelItems.stream()
        .map(map -> convertMapToTravelPlanItemForUpdate(map, travelPlanId))
        .toList();

    return travelPlanItemService.updateByIds(items);
  }

  /**
   * 将Map转换为TravelPlanItem（用于更新，包含id）
   */
  private TravelPlanItem convertMapToTravelPlanItemForUpdate(Map<String, Object> map, Long travelPlanId) {
    TravelPlanItem item = new TravelPlanItem();
    item.setTravelPlanId(travelPlanId);

    // 设置id（更新时必须）
    Object idObj = map.get("id");
    if (idObj != null) {
      if (idObj instanceof Number) {
        item.setId(((Number) idObj).longValue());
      } else if (idObj instanceof String) {
        item.setId(Long.parseLong((String) idObj));
      }
    }

    // 设置itemType
    String itemTypeStr = (String) map.get("itemType");
    if (itemTypeStr != null) {
      item.setItemType(TravelTypeEnum.valueOf(itemTypeStr));
    }

    // 设置时间
    String startTimeStr = (String) map.get("startTime");
    if (startTimeStr != null) {
      item.setStartTime(LocalDateTime.parse(startTimeStr));
    }

    String endTimeStr = (String) map.get("endTime");
    if (endTimeStr != null) {
      item.setEndTime(LocalDateTime.parse(endTimeStr));
    }

    // 设置地点
    item.setFromLocation((String) map.get("fromLocation"));
    item.setToLocation((String) map.get("toLocation"));

    // 设置交通工具
    item.setTransportType((String) map.get("transportType"));

    // 设置金额
    Object amountObj = map.get("amount");
    if (amountObj != null) {
      if (amountObj instanceof Number) {
        item.setAmount(BigDecimal.valueOf(((Number) amountObj).doubleValue()));
      } else if (amountObj instanceof String) {
        item.setAmount(new BigDecimal((String) amountObj));
      }
    }

    return item;
  }

  /**
   * ai批量删除行程环节明细
   * 
   * @param ids 行程环节ID列表
   * @return 操作结果
   */
  @Tool(description = """
      批量删除行程环节。
      参数示例：[168, 169]
      字段说明：
      - ids: 环节ID列表，每个ID是从getByTripId查询结果中获取的id字段
      """)
  public Result delete(
      @ToolParam(description = "行程环节ID列表，例如：[168, 169]") List<Integer> ids) {
    if (CollectionUtils.isEmpty(ids)) {
      return Result.fail("行程环节ID列表不能为空");
    }

    // 构建TravelPlanItem对象列表（将Integer转为Long）
    List<TravelPlanItem> travelItems = ids.stream()
        .map(id -> {
          TravelPlanItem item = new TravelPlanItem();
          item.setId(id.longValue());
          return item;
        })
        .toList();

    return travelPlanItemService.delete(travelItems);
  }

}
