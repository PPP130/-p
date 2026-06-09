package com.sky.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ShoppingCartDTO implements Serializable {

    // 所属店铺编号
    private String shopId;
    private Long dishId;
    private Long setmealId;
    private String dishFlavor;

}
