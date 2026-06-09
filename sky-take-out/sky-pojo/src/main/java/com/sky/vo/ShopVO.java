package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺返回的数据格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "店铺返回的数据格式")
public class ShopVO implements Serializable {

    @ApiModelProperty("主键值")
    private Long id;

    @ApiModelProperty("店铺编号")
    private String shopId;

    @ApiModelProperty("店铺名称")
    private String name;

    @ApiModelProperty("店主姓名")
    private String ownerName;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("店铺地址")
    private String address;

    @ApiModelProperty("店铺图片")
    private String image;

    @ApiModelProperty("店铺简介")
    private String description;

    @ApiModelProperty("审核状态 0待审核 1已通过 2已拒绝 3已禁用")
    private Integer status;

    @ApiModelProperty("营业状态 0打烊 1营业中")
    private Integer businessStatus;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("默认管理员用户名")
    private String username;

    @ApiModelProperty("默认管理员密码")
    private String password;
}
