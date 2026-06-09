package com.sky.controller.admin;

import com.sky.constant.RedisConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @PostMapping
    @ApiOperation("新增菜品")
    @CacheEvict(cacheNames = RedisConstant.DISH_CATEGORY_KEY, key = "#dishDTO.categoryId")
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        dishService.saveWithFlavors(dishDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = RedisConstant.DISH_CATEGORY_KEY, key = "#dishDTO.categoryId")
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        dishService.updateWithFlavors(dishDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    @CacheEvict(cacheNames = RedisConstant.DISH_CATEGORY_KEY, allEntries = true)
    public Result<String> delete(@RequestParam String ids) {
        dishService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishService.listByCategoryId(categoryId);
        return Result.success(list);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    @CacheEvict(cacheNames = RedisConstant.DISH_CATEGORY_KEY, allEntries = true)
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        return Result.success();
    }
}
