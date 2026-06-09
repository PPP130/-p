package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    void saveWithFlavors(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    DishVO getByIdWithFlavors(Long id);

    void updateWithFlavors(DishDTO dishDTO);

    void startOrStop(Integer status, Long id);

    void deleteBatch(String ids);

    List<Dish> listByCategoryId(Long categoryId);

    List<DishVO> listVOByCategoryId(Long categoryId);
}
