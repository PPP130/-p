package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {
    /**
     * 新增员工
     * @param employeeDTO
     */

     void save(EmployeeDTO employeeDTO);


    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 查询员工
     * @param employeePageQueryDTO
     * @return
     */

    PageResult page(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 禁用员工账号
     * @param status
     * @param id
     */

    void startorStop(Integer status, long id);

    /**
     * ID查询
     * @param id
     * @return
     */

    Employee getbyid(long id);

    /**
     * 编辑员工
     * @param employeeDTO
     */

    void updata(EmployeeDTO employeeDTO);

    /**
     * 修改密码
     * @param passwordEditDTO
     */
    void editPassword(PasswordEditDTO passwordEditDTO);
}
