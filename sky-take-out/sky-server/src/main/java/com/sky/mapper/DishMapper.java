package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
    @Insert("insert into dish(shop_id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) " +
            "values(#{shopId}, #{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    DishVO getVOById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    void deleteByIds(List<Long> ids);

    @Update("update dish set status = #{status} where id = #{id}")
    void updateStatus(@Param("status") Integer status, @Param("id") Long id);

    List<Dish> listByCategoryId(@Param("categoryId") Long categoryId, @Param("shopId") String shopId);

    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    @Select("select count(id) from dish where status = #{status} and (shop_id = #{shopId} or #{shopId} is null)")
    Integer countByStatus(@Param("shopId") String shopId, @Param("status") Integer status);
}
