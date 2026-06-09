package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺管理员登录时传递的数据模型
 */
@Data
@ApiModel(description = "店铺管理员登录时传递的数据模型")
public class ShopLoginDTO implements Serializable {

    @ApiModelProperty("店铺编号")
    private String shopId;

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;
}
