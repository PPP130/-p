package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    @Select("select * from shopping_cart where user_id = #{userId} and (shop_id = #{shopId} or #{shopId} is null)")
    List<ShoppingCart> listByUserId(@Param("userId") Long userId, @Param("shopId") String shopId);

    @Select("select * from shopping_cart where user_id = #{userId} and dish_id = #{dishId} and (dish_flavor = #{dishFlavor} or (dish_flavor is null and #{dishFlavor} is null)) and (shop_id = #{shopId} or #{shopId} is null)")
    ShoppingCart getByDishIdAndFlavor(@Param("userId") Long userId, @Param("dishId") Long dishId, @Param("dishFlavor") String dishFlavor, @Param("shopId") String shopId);

    @Select("select * from shopping_cart where user_id = #{userId} and setmeal_id = #{setmealId} and (shop_id = #{shopId} or #{shopId} is null)")
    ShoppingCart getBySetmealId(@Param("userId") Long userId, @Param("setmealId") Long setmealId, @Param("shopId") String shopId);

    @Update("update shopping_cart set number = #{number}, amount = #{amount} where id = #{id}")
    void update(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart(shop_id, name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values(#{shopId}, #{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    @Delete("delete from shopping_cart where user_id = #{userId} and shop_id = #{shopId}")
    void deleteByUserIdAndShopId(@Param("userId") Long userId, @Param("shopId") String shopId);
}
