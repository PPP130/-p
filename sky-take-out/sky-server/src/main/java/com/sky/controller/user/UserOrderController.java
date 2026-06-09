package com.sky.controller.user;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import java.util.Map;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端-订单接口")
public class UserOrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @GetMapping("/userPage")
    @ApiOperation("历史订单分页查询")
    public Result<PageResult> page(Integer page, Integer pageSize,
                                  @RequestParam(required = false) String status) {
        Integer statusInt = null;
        if (status != null && !status.isEmpty()) {
            statusInt = Integer.valueOf(status);
        }
        PageResult pageResult = orderService.pageQuery(page, pageSize, statusInt);
        return Result.success(pageResult);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("历史订单（别名）")
    public Result<PageResult> historyOrders(Integer page, Integer pageSize,
                                            @RequestParam(required = false) String status) {
        Integer statusInt = null;
        if (status != null && !status.isEmpty()) {
            statusInt = Integer.valueOf(status);
        }
        PageResult pageResult = orderService.pageQuery(page, pageSize, statusInt);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        OrderVO orderVO = orderService.getOrderDetail(id);
        return Result.success(orderVO);
    }

    @GetMapping("/again/{id}")
    @ApiOperation("再来一单")
    public Result again(@PathVariable Long id) {
        orderService.repeatOrder(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单（前端调用）")
    public Result repetition(@PathVariable Long id) {
        orderService.repeatOrder(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success();
    }

    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result payment(@RequestBody Map<String, Object> params) {
        String orderNumber = (String) params.get("orderNumber");
        orderService.payOrder(orderNumber);
        return Result.success();
    }
}
