package com.liu.lutu.controller;

import com.liu.lutu.domain.po.Result;
import com.liu.lutu.service.ITravelPlanFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 行程收藏 前端控制器
 */
@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Tag(name = "行程收藏", description = "行程收藏管理")
public class TravelPlanFavoriteController {

    private final ITravelPlanFavoriteService favoriteService;

    /**
     * 收藏/取消收藏行程
     */
    @Operation(summary = "收藏/取消收藏", description = "切换收藏状态")
    @PostMapping("/toggle/{shareId}")
    public Result<Boolean> toggleFavorite(@PathVariable Long shareId) {
        return favoriteService.toggleFavorite(shareId);
    }

    /**
     * 检查是否已收藏
     */
    @Operation(summary = "检查收藏状态", description = "检查当前用户是否已收藏该行程")
    @GetMapping("/check/{shareId}")
    public Result<Boolean> checkFavorite(@PathVariable Long shareId) {
        return Result.success(favoriteService.hasFavorited(shareId));
    }

    /**
     * 获取我的收藏列表
     */
    @Operation(summary = "我的收藏", description = "获取当前用户的收藏列表")
    @GetMapping("/my")
    public Result getMyFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return favoriteService.getMyFavorites(page, size);
    }

    /**
     * 克隆行程（复制到自己行程）
     */
    @Operation(summary = "克隆行程", description = "将分享的行程复制为自己的行程")
    @PostMapping("/clone/{shareId}")
    public Result<Long> clonePlan(@PathVariable Long shareId) {
        return favoriteService.clonePlan(shareId);
    }
}
