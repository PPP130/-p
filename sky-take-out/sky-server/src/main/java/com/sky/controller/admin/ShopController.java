package com.sky.controller.admin;

import com.sky.dto.ShopDTO;
import com.sky.dto.ShopPageQueryDTO;
import com.sky.entity.Shop;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.ShopService;
import com.sky.vo.ShopVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺管理（管理端）
 */
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺管理接口")
@Slf4j
public class ShopController {

    @Autowired
    private ShopService shopService;

    // ========== 原有功能：全局营业状态 ==========

    @PutMapping("/status")
    @ApiOperation("设置营业状态")
    public Result<String> setStatus(@RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("营业状态参数不合法");
        }
        shopService.setStatus(status);
        return Result.success();
    }

    // ========== 新增功能：多店铺管理 ==========

    /**
     * 店铺注册（加盟申请）
     */
    @PostMapping("/register")
    @ApiOperation("店铺注册")
    public Result<ShopVO> register(@RequestBody ShopDTO shopDTO) {
        log.info("店铺注册：{}", shopDTO);
        ShopVO shopVO = shopService.register(shopDTO);
        return Result.success(shopVO);
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @ApiOperation("店铺分页查询")
    public Result<PageResult> page(ShopPageQueryDTO shopPageQueryDTO) {
        log.info("店铺分页查询：{}", shopPageQueryDTO);
        PageResult pageResult = shopService.pageQuery(shopPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 审核通过
     */
    @PutMapping("/approve/{shopId}")
    @ApiOperation("审核通过")
    public Result approve(@PathVariable String shopId) {
        log.info("审核通过：{}", shopId);
        shopService.approve(shopId);
        return Result.success();
    }

    /**
     * 审核拒绝
     */
    @PutMapping("/reject/{shopId}")
    @ApiOperation("审核拒绝")
    public Result reject(@PathVariable String shopId) {
        log.info("审核拒绝：{}", shopId);
        shopService.reject(shopId);
        return Result.success();
    }

    /**
     * 禁用店铺
     */
    @PutMapping("/disable/{shopId}")
    @ApiOperation("禁用店铺")
    public Result disable(@PathVariable String shopId) {
        log.info("禁用店铺：{}", shopId);
        shopService.disable(shopId);
        return Result.success();
    }

    /**
     * 查询店铺详情
     */
    @GetMapping("/info/{shopId}")
    @ApiOperation("查询店铺详情")
    public Result<Shop> getByShopId(@PathVariable String shopId) {
        Shop shop = shopService.getByShopId(shopId);
        return Result.success(shop);
    }

    /**
     * 更新店铺信息
     */
    @PutMapping("/info/{shopId}")
    @ApiOperation("更新店铺信息")
    public Result update(@PathVariable String shopId, @RequestBody ShopDTO shopDTO) {
        log.info("更新店铺信息：shopId={}, data={}", shopId, shopDTO);
        shopService.update(shopDTO, shopId);
        return Result.success();
    }

    /**
     * 设置店铺营业状态
     */
    @PutMapping("/businessStatus")
    @ApiOperation("设置店铺营业状态")
    public Result setBusinessStatus(@RequestParam String shopId, @RequestParam Integer status) {
        log.info("设置店铺营业状态：shopId={}, status={}", shopId, status);
        shopService.setBusinessStatus(shopId, status);
        return Result.success();
    }
}
