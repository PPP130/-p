package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    // 店铺编号（随机生成）
    private String shopId;

    // 店铺名称
    private String name;

    // 店主姓名
    private String ownerName;

    // 联系电话
    private String phone;

    // 店铺地址
    private String address;

    // 店铺图片
    private String image;

    // 店铺简介
    private String description;

    // 审核状态 0待审核 1已通过 2已拒绝 3已禁用
    private Integer status;

    // 营业状态 0打烊 1营业中
    private Integer businessStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
