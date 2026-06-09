package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        String shopId = BaseContext.getCurrentShopId();
        return shoppingCartMapper.listByUserId(userId, shopId);
    }

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        String shopId = shoppingCartDTO.getShopId();
        if (shopId == null || shopId.isEmpty()) {
            shopId = BaseContext.getCurrentShopId();
        }

        ShoppingCart cart;
        if (shoppingCartDTO.getDishId() != null) {
            cart = shoppingCartMapper.getByDishIdAndFlavor(userId, shoppingCartDTO.getDishId(), shoppingCartDTO.getDishFlavor(), shopId);
        } else {
            cart = shoppingCartMapper.getBySetmealId(userId, shoppingCartDTO.getSetmealId(), shopId);
        }

        if (cart != null) {
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
            return;
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        shoppingCart.setShopId(shopId);

        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }

        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        String shopId = shoppingCartDTO.getShopId();
        if (shopId == null || shopId.isEmpty()) {
            shopId = BaseContext.getCurrentShopId();
        }

        ShoppingCart cart;
        if (shoppingCartDTO.getDishId() != null) {
            cart = shoppingCartMapper.getByDishIdAndFlavor(userId, shoppingCartDTO.getDishId(), shoppingCartDTO.getDishFlavor(), shopId);
        } else {
            cart = shoppingCartMapper.getBySetmealId(userId, shoppingCartDTO.getSetmealId(), shopId);
        }

        if (cart != null) {
            if (cart.getNumber() > 1) {
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.update(cart);
            } else {
                shoppingCartMapper.deleteById(cart.getId());
            }
        }
    }

    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        String shopId = BaseContext.getCurrentShopId();
        if (shopId != null && !shopId.isEmpty()) {
            shoppingCartMapper.deleteByUserIdAndShopId(userId, shopId);
        } else {
            shoppingCartMapper.deleteByUserId(userId);
        }
    }
}
