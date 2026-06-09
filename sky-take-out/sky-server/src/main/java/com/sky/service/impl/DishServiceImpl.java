package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Transactional
    public void saveWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 自动设置 shopId
        if (dish.getShopId() == null || dish.getShopId().isEmpty()) {
            dish.setShopId(BaseContext.getCurrentShopId());
        }

        dishMapper.insert(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(f -> f.setDishId(dish.getId()));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 自动过滤当前店铺数据
        if (dishPageQueryDTO.getShopId() == null || dishPageQueryDTO.getShopId().isEmpty()) {
            String currentShopId = BaseContext.getCurrentShopId();
            if (currentShopId != null && !currentShopId.isEmpty()) {
                dishPageQueryDTO.setShopId(currentShopId);
            }
        }
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    public DishVO getByIdWithFlavors(Long id) {
        DishVO dishVO = dishMapper.getVOById(id);
        List<DishFlavor> flavors = dishFlavorMapper.listByDishId(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Transactional
    public void updateWithFlavors(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        Long dishId = dishDTO.getId();
        dishFlavorMapper.deleteByDishId(dishId);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(f -> f.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    public void startOrStop(Integer status, Long id) {
        dishMapper.updateStatus(status, id);
    }

    @Transactional
    public void deleteBatch(String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());

        dishFlavorMapper.deleteByDishIds(idList);
        dishMapper.deleteByIds(idList);
    }

    public List<Dish> listByCategoryId(Long categoryId) {
        String shopId = BaseContext.getCurrentShopId();
        return dishMapper.listByCategoryId(categoryId, shopId);
    }

    @Cacheable(cacheNames = "dish_category_", key = "#categoryId")
    public List<DishVO> listVOByCategoryId(Long categoryId) {
        String shopId = BaseContext.getCurrentShopId();
        List<Dish> dishes = dishMapper.listByCategoryId(categoryId, shopId);
        List<DishVO> dishVOList = new java.util.ArrayList<>();
        for (Dish dish : dishes) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            List<DishFlavor> flavors = dishFlavorMapper.listByDishId(dish.getId());
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }
}
