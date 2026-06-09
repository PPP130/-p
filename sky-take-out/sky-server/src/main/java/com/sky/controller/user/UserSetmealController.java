package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐接口")
public class UserSetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @GetMapping("/list")
    @ApiOperation("根据分类查询套餐")
    public Result<List<Setmeal>> list(Long categoryId) {
        List<Setmeal> list = setmealService.listByCategoryId(categoryId);
        return Result.success(list);
    }

    @GetMapping("/dish/deatils")
    @ApiOperation("根据套餐id查询包含的菜品")
    public Result<List<DishItemVO>> dishDetails(Long id) {
        List<SetmealDish> setmealDishes = setmealDishMapper.listBySetmealId(id);
        List<DishItemVO> dishItemVOList = new ArrayList<>();
        for (SetmealDish setmealDish : setmealDishes) {
            DishItemVO dishItemVO = new DishItemVO();
            BeanUtils.copyProperties(setmealDish, dishItemVO);
            dishItemVOList.add(dishItemVO);
        }
        return Result.success(dishItemVOList);
    }
}
