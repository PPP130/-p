package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺注册/编辑时传递的数据模型
 */
@Data
@ApiModel(description = "店铺注册时传递的数据模型")
public class ShopDTO implements Serializable {

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
}
