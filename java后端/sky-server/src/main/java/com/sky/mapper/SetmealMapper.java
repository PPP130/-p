package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
    @Insert("insert into setmeal(shop_id, category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) " +
            "values(#{shopId}, #{categoryId}, #{name}, #{price}, #{status}, #{description}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO getVOById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    void deleteByIds(List<Long> ids);

    void updateStatus(@Param("status") Integer status, @Param("id") Long id);

    Integer countByIdsAndStatus(@Param("list") List<Long> ids, @Param("status") Integer status);

    Integer countDisableDishBySetmealId(@Param("setmealId") Long setmealId);

    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    @Select("select * from setmeal where category_id = #{categoryId} and status = 1 and (shop_id = #{shopId} or #{shopId} is null)")
    List<Setmeal> listByCategoryId(@Param("categoryId") Long categoryId, @Param("shopId") String shopId);

    @Select("select count(id) from setmeal where status = #{status} and (shop_id = #{shopId} or #{shopId} is null)")
    Integer countByStatus(@Param("shopId") String shopId, @Param("status") Integer status);
}
