package com.sky.controller.user;

import com.sky.entity.Shop;
import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺相关接口（用户端）
 */
@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "C端-店铺接口")
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 获取全局营业状态（兼容原有逻辑）
     */
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getStatus() {
        Integer status = shopService.getStatus();
        return Result.success(status);
    }

    /**
     * 获取指定店铺营业状态
     */
    @GetMapping("/{shopId}/status")
    @ApiOperation("获取店铺营业状态")
    public Result<Integer> getBusinessStatus(@PathVariable String shopId) {
        Integer status = shopService.getBusinessStatus(shopId);
        return Result.success(status);
    }

    /**
     * 获取已审核通过的店铺列表（用户端选择店铺）
     */
    @GetMapping("/list")
    @ApiOperation("获取店铺列表")
    public Result<List<Shop>> list() {
        List<Shop> list = shopService.listApproved();
        return Result.success(list);
    }

    /**
     * 获取店铺详情（用户端回显店铺信息）
     */
    @GetMapping("/{shopId}/info")
    @ApiOperation("获取店铺详情")
    public Result<Shop> getShopInfo(@PathVariable String shopId) {
        Shop shop = shopService.getByShopId(shopId);
        return Result.success(shop);
    }
}
