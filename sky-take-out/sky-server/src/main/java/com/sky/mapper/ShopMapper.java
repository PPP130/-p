package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.ShopPageQueryDTO;
import com.sky.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopMapper {

    /**
     * 插入店铺
     */
    void insert(Shop shop);

    /**
     * 根据 shopId 查询店铺
     */
    @Select("select * from shop where shop_id = #{shopId}")
    Shop getByShopId(String shopId);

    /**
     * 分页查询
     */
    Page<Shop> pageQuery(ShopPageQueryDTO shopPageQueryDTO);

    /**
     * 更新店铺信息
     */
    void update(Shop shop);

    /**
     * 根据 shopId 更新状态
     */
    @Select("update shop set status = #{status} where shop_id = #{shopId}")
    void updateStatus(String shopId, Integer status);

    /**
     * 查询所有已审核通过的店铺
     */
    List<Shop> listApproved();
}
