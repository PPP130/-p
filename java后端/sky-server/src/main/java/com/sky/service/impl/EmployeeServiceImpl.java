package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;
    /**
     * 新增员工
     * @param employeeDTO
     */

    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        // 自动设置 shopId：优先用 DTO 传入的，否则从 BaseContext 获取
        if (employee.getShopId() == null || employee.getShopId().isEmpty()) {
            employee.setShopId(BaseContext.getCurrentShopId());
        }
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        Long currentId = BaseContext.getCurrentId();
        employee.setCreateUser(currentId);
        employee.setUpdateUser(currentId);
        employeeMapper.insert(employee);
    }

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();
        String shopId = employeeLoginDTO.getShopId();

        //1、根据用户名查询数据库中的数据
        Employee employee;
        if (shopId != null && !shopId.isEmpty()) {
            // 店铺管理员登录：按 shopId + username 查询
            employee = employeeMapper.getByUsernameAndShopId(username, shopId);
        } else {
            // 平台管理员登录：按 username 查询
            employee = employeeMapper.getByUsername(username);
        }

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }
    /**
     * 查询员工
     */
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        // 如果 DTO 没有指定 shopId，自动从 BaseContext 获取（店铺管理员只能看自己店铺的员工）
        if (employeePageQueryDTO.getShopId() == null || employeePageQueryDTO.getShopId().isEmpty()) {
            String currentShopId = BaseContext.getCurrentShopId();
            if (currentShopId != null && !currentShopId.isEmpty()) {
                employeePageQueryDTO.setShopId(currentShopId);
            }
        }
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.page(employeePageQueryDTO);
        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }
    /**
     * 禁用员工账号
     */

    public void startorStop(Integer status, long id) {
    Employee employee = new Employee();
    employee.setId(id);
    employee.setStatus(status);
    employeeMapper.update(employee);
    }

    /**
     * id查询
     */
    public Employee getbyid(long id){
        Employee employee=employeeMapper.getbyid(id);
        return employee;
    }

    /**
     * 编辑员工
     * @param employeeDTO
     */

    public void updata(EmployeeDTO employeeDTO) {
    Employee employee = new Employee();
    BeanUtils.copyProperties(employeeDTO, employee);
    employee.setUpdateTime(LocalDateTime.now());
    employee.setUpdateUser(BaseContext.getCurrentId());
    employee.setPassword("*****");
    employeeMapper.update(employee);
    }

    /**
     * 修改密码
     */
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Long empId = passwordEditDTO.getEmpId();
        Employee employee = employeeMapper.getbyid(empId);
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 校验旧密码
        String oldPwd = DigestUtils.md5DigestAsHex(passwordEditDTO.getOldPassword().getBytes());
        if (!oldPwd.equals(employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        // 更新新密码
        Employee updateEmp = new Employee();
        updateEmp.setId(empId);
        updateEmp.setPassword(DigestUtils.md5DigestAsHex(passwordEditDTO.getNewPassword().getBytes()));
        updateEmp.setUpdateTime(LocalDateTime.now());
        updateEmp.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(updateEmp);
    }
}
