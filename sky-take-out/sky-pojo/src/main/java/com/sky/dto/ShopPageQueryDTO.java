package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 店铺分页查询 DTO
 */
@Data
public class ShopPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    // 店铺名称（模糊查询）
    private String name;

    // 审核状态
    private Integer status;
}
