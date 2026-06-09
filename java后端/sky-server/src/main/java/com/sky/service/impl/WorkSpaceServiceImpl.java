package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Orders;
import com.sky.mapper.*;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public BusinessDataVO getBusinessData() {
        LocalDateTime begin = LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDateTime.now().toLocalDate(), LocalTime.MAX);
        String shopId = BaseContext.getCurrentShopId();

        BigDecimal turnover = orderMapper.sumByStatusAndTime(shopId, begin, end);
        turnover = turnover == null ? BigDecimal.ZERO : turnover;

        Integer validOrderCount = orderMapper.countValidByTime(shopId, begin, end);

        Integer totalOrderCount = orderMapper.countByTime(shopId, begin, end);
        double orderCompletionRate = totalOrderCount == 0 ? 0 : validOrderCount.doubleValue() / totalOrderCount;

        double unitPrice = validOrderCount == 0 ? 0 : turnover.doubleValue() / validOrderCount;

        Integer newUsers = userMapper.countByCreateTime(begin, end);

        return BusinessDataVO.builder()
                .turnover(turnover.doubleValue())
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    @Override
    public DishOverViewVO getDishOverView() {
        String shopId = BaseContext.getCurrentShopId();
        Integer sold = dishMapper.countByStatus(shopId, StatusConstant.ENABLE);
        Integer discontinued = dishMapper.countByStatus(shopId, StatusConstant.DISABLE);
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {
        String shopId = BaseContext.getCurrentShopId();
        Integer sold = setmealMapper.countByStatus(shopId, StatusConstant.ENABLE);
        Integer discontinued = setmealMapper.countByStatus(shopId, StatusConstant.DISABLE);
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    @Override
    public OrderOverViewVO getOrderOverView() {
        String shopId = BaseContext.getCurrentShopId();
        Integer waitingOrders = orderMapper.countByStatus(shopId, Orders.TO_BE_CONFIRMED);
        Integer deliveredOrders = orderMapper.countByStatus(shopId, Orders.CONFIRMED);
        Integer completedOrders = orderMapper.countByStatus(shopId, Orders.COMPLETED);
        Integer cancelledOrders = orderMapper.countByStatus(shopId, Orders.CANCELLED);
        Integer allOrders = waitingOrders + deliveredOrders + completedOrders + cancelledOrders
                + orderMapper.countByStatus(shopId, Orders.PENDING_PAYMENT)
                + orderMapper.countByStatus(shopId, Orders.DELIVERY_IN_PROGRESS);
        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }
}
