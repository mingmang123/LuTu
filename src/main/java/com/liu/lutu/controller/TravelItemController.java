package com.liu.lutu.controller;


import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlanItem;
import com.liu.lutu.domain.vo.TravelPlanItemVo;
import com.liu.lutu.service.ITravelPlanItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 旅行项目表（交通/游玩/住宿） 前端控制器
 * 记录用户的出行环节计划
 * </p>
 *
 * @author liu
 * @since 2026-04-03
 */
@RestController
@RequestMapping("/travelPlanItem")
@RequiredArgsConstructor
@Tag(name = "旅行计划环节管理", description = "旅行计划环节管理")
@Slf4j
public class TravelItemController {
    private final ITravelPlanItemService travelPlanItemService;

    /**
     * 查询用户的出行计划环节
     * @param tripId 旅行ID
     * @return
     */
    @GetMapping("/get")
    @Operation(summary = "旅行计划环节查询", description = "旅行计划环节查询")
    public Result<List<TravelPlanItemVo>> get(Long tripId) {
        return travelPlanItemService.getByTripId(tripId);

    }

    /**
     * 添加用户的出行计划环节
     * @param travelItem 出行计划
     * @return
     */
    @PostMapping("/add")
    @Operation(summary = "旅行计划环节添加", description = "旅行计划环节添加")
    public Result add(@RequestBody List<TravelPlanItem> travelItem) {
        return travelPlanItemService.adds(travelItem);
    }

    /**
     * 更新用户的出行计划环节
     * @param travelItem 出行计划
     * @return
     */
    @PutMapping("/update")
    @Operation(summary = "旅行计划环节更新", description = "旅行计划环节更新")
    public Result update(@RequestBody List<TravelPlanItem> travelItem) {
        return travelPlanItemService.updateByIds(travelItem);
    }

    /**
     * 删除用户的出行计划环节
     * @param travelItem 出行计划
     * @return
     */
    @DeleteMapping("/delete")
    @Operation(summary = "旅行计划环节删除", description = "旅行计划环节删除")
    public Result delete(@RequestBody List<TravelPlanItem> travelItem) {
        return travelPlanItemService.delete(travelItem);
    }


}
