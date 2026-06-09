package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        // 如果员工关联了店铺，将 shopId 放入 JWT
        if (employee.getShopId() != null) {
            claims.put(JwtClaimsConstant.SHOP_ID, employee.getShopId());
        }
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .shopId(employee.getShopId())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();

    }

    /**
     *新增员工
     * @param employeeDTO
     * @return
     */
    @ApiOperation("新增员工")
    @PostMapping
    public Result save (@RequestBody EmployeeDTO employeeDTO) {
    log.info("新增员工{}", employeeDTO);
    employeeService.save(employeeDTO);
    return Result.success();

    }
    /**
     * 查询员工
     */
    @ApiOperation("员工查询")
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("查询员工{}", employeePageQueryDTO);
        PageResult pageResult= employeeService.page(employeePageQueryDTO);

        return Result.success(pageResult);
    }
    /**
     * 禁用员工账号
     */
    @ApiOperation("禁用员工账号")
    @PostMapping("/status/{status}")
    public Result startorStop(@PathVariable Integer status,long id){
        log.info("禁用账号{}{} ", status,id);
        employeeService.startorStop(status,id);
        return Result.success();

    }
    /**
     * 根据id查询
     */
    @ApiOperation("id查询")
    @GetMapping("/{id}")
    public Result<Employee> getbyid(@PathVariable long id) {
        log.info("id查询{}", id);
        Employee data =employeeService.getbyid(id);
        return Result.success(data);
    }

    /**
     * 编辑员工
     */
    @ApiOperation("编辑员工")
    @PutMapping()
    public Result updata(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工{}", employeeDTO);
        employeeService.updata(employeeDTO);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @ApiOperation("修改密码")
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("修改密码{}", passwordEditDTO);
        employeeService.editPassword(passwordEditDTO);
        return Result.success();
    }
}
