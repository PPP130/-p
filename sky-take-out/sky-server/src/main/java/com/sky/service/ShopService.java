package com.sky.service;

import com.sky.dto.ShopDTO;
import com.sky.dto.ShopPageQueryDTO;
import com.sky.entity.Shop;
import com.sky.result.PageResult;
import com.sky.vo.ShopVO;

import java.util.List;

public interface ShopService {

    // ========== 原有功能：全局营业状态（Redis） ==========

    void setStatus(Integer status);

    Integer getStatus();

    // ========== 新增功能：多店铺管理 ==========

    /**
     * 店铺注册（加盟）
     */
    ShopVO register(ShopDTO shopDTO);

    /**
     * 根据 shopId 查询店铺
     */
    Shop getByShopId(String shopId);

    /**
     * 分页查询
     */
    PageResult pageQuery(ShopPageQueryDTO shopPageQueryDTO);

    /**
     * 审核通过
     */
    void approve(String shopId);

    /**
     * 审核拒绝
     */
    void reject(String shopId);

    /**
     * 禁用店铺
     */
    void disable(String shopId);

    /**
     * 设置店铺营业状态
     */
    void setBusinessStatus(String shopId, Integer status);

    /**
     * 获取店铺营业状态
     */
    Integer getBusinessStatus(String shopId);

    /**
     * 更新店铺信息
     */
    void update(ShopDTO shopDTO, String shopId);

    /**
     * 获取所有已审核通过的店铺列表（用户端）
     */
    List<Shop> listApproved();
}
