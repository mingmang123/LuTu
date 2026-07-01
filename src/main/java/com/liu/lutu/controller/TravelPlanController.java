package com.liu.lutu.controller;


import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelPlan;
import com.liu.lutu.domain.vo.TravelPlanVo;
import com.liu.lutu.service.ITravelPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 旅行总表 前端控制器
 * </p>
 *
 * @author liu
 * @since 2026-04-03
 */
@RestController
@RequestMapping("/travelplan")
@Tag(name = "旅行计划管理", description = "旅行计划管理")
@Slf4j
@RequiredArgsConstructor
public class TravelPlanController {

    private final ITravelPlanService travelPlanService;

    /**
     * 查询用户的旅行计划
     * @return
     */
    @GetMapping("/get")
    @Operation(summary = "旅行计划查询", description = "旅行计划查询")
    public Result<List<TravelPlanVo>> get() {
        return travelPlanService.getByUserId();
    }


    /**
     * 添加用户的旅行计划
     * @param travelPlan 旅行计划
     * @return
     */
    @PostMapping("/add")
    @Operation(summary = "旅行计划添加", description = "旅行计划添加")
    public Result add(@RequestBody TravelPlan travelPlan) {
        return travelPlanService.saveByUserId(travelPlan);
    }

    /**
     * 删除用户的旅行计划
     * @param id 旅行计划ID
     * @return
     */
    @DeleteMapping("/delete")
    @Operation(summary = "旅行计划删除", description = "旅行计划删除")
    public Result delete(Long id) {
        return travelPlanService.removeByUserId(id);
    }


}
