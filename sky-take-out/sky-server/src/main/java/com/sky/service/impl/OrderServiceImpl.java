package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        String shopId = ordersSubmitDTO.getShopId();
        if (shopId == null || shopId.isEmpty()) {
            shopId = BaseContext.getCurrentShopId();
        }
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        User user = userMapper.getById(userId);

        List<ShoppingCart> cartList = shoppingCartMapper.listByUserId(userId, shopId);
        if (cartList == null || cartList.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setShopId(shopId);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);

        shoppingCartMapper.deleteByUserId(userId);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        // 通过 WebSocket 向管理端推送新订单通知
        WebSocketServer.sendToAllClient("{\"type\": 1, \"content\": \"你有新的订单\"}");

        return orderSubmitVO;
    }

    @Override
    public PageResult pageQuery(Integer page, Integer pageSize, Integer status) {
        PageHelper.startPage(page, pageSize);
        Orders orders = new Orders();
        orders.setUserId(BaseContext.getCurrentId());
        // 当 status 为 null 时查询所有状态的订单
        if (status != null) {
            orders.setStatus(status);
        }
        Page<Orders> orderPage = orderMapper.pageQuery(orders);

        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders o : orderPage.getResult()) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(o, orderVO);
            List<OrderDetail> details = orderDetailMapper.listByOrderId(o.getId());
            orderVO.setOrderDetailList(details);
            // 拼接菜品信息
            StringBuilder orderDishes = new StringBuilder();
            for (OrderDetail detail : details) {
                orderDishes.append(detail.getName()).append("x").append(detail.getNumber()).append(",");
            }
            if (orderDishes.length() > 0) {
                orderDishes.deleteCharAt(orderDishes.length() - 1);
            }
            orderVO.setOrderDishes(orderDishes.toString());
            orderVOList.add(orderVO);
        }
        return new PageResult(orderPage.getTotal(), orderVOList);
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> details = orderDetailMapper.listByOrderId(id);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    @Override
    @Transactional
    public void repeatOrder(Long id) {
        Orders order = orderMapper.getById(id);
        List<OrderDetail> details = orderDetailMapper.listByOrderId(id);
        Long userId = BaseContext.getCurrentId();
        for (OrderDetail detail : details) {
            ShoppingCart cart = new ShoppingCart();
            BeanUtils.copyProperties(detail, cart);
            cart.setUserId(userId);
            cart.setShopId(order.getShopId());
            cart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(cart);
        }
    }

    @Override
    public void cancel(Long id) {
        Orders orders = orderMapper.getById(id);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void payOrder(String orderNumber) {
        Orders orders = orderMapper.getByNumber(orderNumber);
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setPayStatus(Orders.PAID);
        orders.setPayMethod(1);
        orders.setCheckoutTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        // 通过 WebSocket 向管理端推送催单通知
        WebSocketServer.sendToAllClient("{\"type\": 2, \"content\": \"订单号:" + orders.getNumber() + " 催单\", \"orderId\": " + id + "}");
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 自动过滤当前店铺数据
        if (ordersPageQueryDTO.getShopId() == null || ordersPageQueryDTO.getShopId().isEmpty()) {
            String currentShopId = BaseContext.getCurrentShopId();
            if (currentShopId != null && !currentShopId.isEmpty()) {
                ordersPageQueryDTO.setShopId(currentShopId);
            }
        }
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersPageQueryDTO, orders);
        Page<Orders> page = orderMapper.pageQuery(orders);

        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders o : page.getResult()) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(o, orderVO);
            List<OrderDetail> details = orderDetailMapper.listByOrderId(o.getId());
            orderVO.setOrderDetailList(details);
            orderVOList.add(orderVO);
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> details = orderDetailMapper.listByOrderId(id);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
    }

    @Override
    public OrderStatisticsVO statistics() {
        String shopId = BaseContext.getCurrentShopId();
        Integer toBeConfirmed = orderMapper.countByStatus(shopId, Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countByStatus(shopId, Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(shopId, Orders.DELIVERY_IN_PROGRESS);
        return OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmed)
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
    }
}
