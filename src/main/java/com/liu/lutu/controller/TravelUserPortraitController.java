package com.liu.lutu.controller;


import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.po.TravelUserPortrait;
import com.liu.lutu.domain.vo.TravelUserPortraitVo;
import com.liu.lutu.service.ITravelUserPortraitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *用户画像
 * @author liu
 * @since 2026-04-18
 */
@RestController
@RequestMapping("/traveluserportrait")
@Slf4j
@RequiredArgsConstructor
public class TravelUserPortraitController {
    private final ITravelUserPortraitService travelUserPortraitService;

    /**
     * 获取用户画像
     * @return
     */
    @GetMapping("/get")
    public Result<TravelUserPortraitVo> get() {
        return travelUserPortraitService.getByUserId();
    }

    /**
     * 更新用户画像
     * @param travelUserPortrait
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody TravelUserPortrait travelUserPortrait) {
        return travelUserPortraitService.updateByUserId(travelUserPortrait);
    }


    /**
     * 添加用户画像
     * @param travelUserPortrait
     * @return
     */
    @PutMapping
    public Result add(@RequestBody TravelUserPortrait travelUserPortrait) {
        return travelUserPortraitService.add(travelUserPortrait);
    }

}
