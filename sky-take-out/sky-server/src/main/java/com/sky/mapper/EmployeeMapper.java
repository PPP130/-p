package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @Select("select * from employee where username = #{username} and shop_id = #{shopId}")
    Employee getByUsernameAndShopId(@Param("username") String username, @Param("shopId") String shopId);
    /**
     * 新增员工
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into employee(shop_id, username, name, `password`, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "values(#{shopId}, #{username}, #{name}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Employee employee);

    /**
     * 查询员工
     */

    Page<Employee> page(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 更新员工
     */
    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    /**
     * id查询
     */
    @Select("SELECT * FROM employee where id =#{id}")
    Employee getbyid(long id);
}