package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders orders);

    Page<Orders> pageQuery(Orders orders);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select * from orders where number = #{number}")
    Orders getByNumber(String number);

    void update(Orders orders);

    Integer countByStatus(@Param("shopId") String shopId, @Param("status") Integer status);

    @Select("select count(id) from orders where status = #{status} and order_time between #{begin} and #{end} and (shop_id = #{shopId} or #{shopId} is null)")
    Integer countByStatusAndTime(@Param("shopId") String shopId, @Param("status") Integer status, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    @Select("select ifnull(sum(amount), 0) from orders where status = 5 and order_time between #{begin} and #{end} and (shop_id = #{shopId} or #{shopId} is null)")
    BigDecimal sumByStatusAndTime(@Param("shopId") String shopId, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    @Select("select count(id) from orders where status = 5 and order_time between #{begin} and #{end} and (shop_id = #{shopId} or #{shopId} is null)")
    Integer countValidByTime(@Param("shopId") String shopId, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    List<Map<String, Object>> countByDateRange(@Param("shopId") String shopId, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    List<Map<String, Object>> sumByDateRange(@Param("shopId") String shopId, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    @Select("select count(id) from orders where order_time between #{begin} and #{end} and (shop_id = #{shopId} or #{shopId} is null)")
    Integer countByTime(@Param("shopId") String shopId, @Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);
}
