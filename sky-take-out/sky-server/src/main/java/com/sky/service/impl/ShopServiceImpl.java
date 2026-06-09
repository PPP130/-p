package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.PasswordConstant;
import com.sky.constant.RedisConstant;
import com.sky.dto.ShopDTO;
import com.sky.dto.ShopPageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.Shop;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.ShopMapper;
import com.sky.result.PageResult;
import com.sky.service.ShopService;
import com.sky.vo.ShopVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // ========== 原有功能：全局营业状态（Redis） ==========

    @Override
    public void setStatus(Integer status) {
        stringRedisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS, String.valueOf(status));
    }

    @Override
    public Integer getStatus() {
        String value = stringRedisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS);
        if (value == null || value.isEmpty()) {
            return 1;
        }
        return Integer.valueOf(value);
    }

    // ========== 新增功能：多店铺管理 ==========

    @Override
    public ShopVO register(ShopDTO shopDTO) {
        Shop shop = new Shop();
        BeanUtils.copyProperties(shopDTO, shop);

        // 生成随机店铺编号：SJ + 日期(yyyyMMdd) + 4位随机十六进制
        String shopId = generateShopId();
        shop.setShopId(shopId);
        shop.setStatus(0); // 待审核
        shop.setBusinessStatus(1); // 默认营业中
        shop.setCreateTime(LocalDateTime.now());
        shop.setUpdateTime(LocalDateTime.now());

        shopMapper.insert(shop);

        // 创建默认管理员账户：admin / 123456
        Employee admin = new Employee();
        admin.setShopId(shopId);
        admin.setUsername("admin");
        admin.setName(shopDTO.getOwnerName() != null ? shopDTO.getOwnerName() : "管理员");
        admin.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        admin.setPhone(shopDTO.getPhone());
        admin.setSex("0");
        admin.setIdNumber("");
        admin.setStatus(1);
        admin.setCreateUser(0L);
        admin.setUpdateUser(0L);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        employeeMapper.insert(admin);

        ShopVO shopVO = new ShopVO();
        BeanUtils.copyProperties(shop, shopVO);
        shopVO.setUsername("admin");
        shopVO.setPassword("123456");
        return shopVO;
    }

    @Override
    public Shop getByShopId(String shopId) {
        return shopMapper.getByShopId(shopId);
    }

    @Override
    public PageResult pageQuery(ShopPageQueryDTO shopPageQueryDTO) {
        PageHelper.startPage(shopPageQueryDTO.getPage(), shopPageQueryDTO.getPageSize());
        Page<Shop> page = shopMapper.pageQuery(shopPageQueryDTO);
        List<ShopVO> voList = page.getResult().stream().map(shop -> {
            ShopVO vo = new ShopVO();
            BeanUtils.copyProperties(shop, vo);
            return vo;
        }).collect(Collectors.toList());
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    public void approve(String shopId) {
        Shop shop = new Shop();
        shop.setShopId(shopId);
        shop.setStatus(1);
        shop.setUpdateTime(LocalDateTime.now());
        shopMapper.update(shop);
    }

    @Override
    public void reject(String shopId) {
        Shop shop = new Shop();
        shop.setShopId(shopId);
        shop.setStatus(2);
        shop.setUpdateTime(LocalDateTime.now());
        shopMapper.update(shop);
    }

    @Override
    public void disable(String shopId) {
        Shop shop = new Shop();
        shop.setShopId(shopId);
        shop.setStatus(3);
        shop.setUpdateTime(LocalDateTime.now());
        shopMapper.update(shop);
    }

    @Override
    public void setBusinessStatus(String shopId, Integer status) {
        Shop shop = new Shop();
        shop.setShopId(shopId);
        shop.setBusinessStatus(status);
        shop.setUpdateTime(LocalDateTime.now());
        shopMapper.update(shop);
    }

    @Override
    public Integer getBusinessStatus(String shopId) {
        Shop shop = shopMapper.getByShopId(shopId);
        if (shop == null || shop.getBusinessStatus() == null) {
            return getStatus(); // fallback 到全局状态
        }
        return shop.getBusinessStatus();
    }

    @Override
    public void update(ShopDTO shopDTO, String shopId) {
        Shop shop = new Shop();
        BeanUtils.copyProperties(shopDTO, shop);
        shop.setShopId(shopId);
        shop.setUpdateTime(LocalDateTime.now());
        shopMapper.update(shop);
    }

    @Override
    public List<Shop> listApproved() {
        return shopMapper.listApproved();
    }

    /**
     * 生成店铺编号：SJ + 日期(yyyyMMdd) + 4位随机十六进制
     * 例：SJ20260513A3F7
     */
    private String generateShopId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String hex = String.format("%04X", (int) (Math.random() * 0xFFFF));
        return "SJ" + date + hex;
    }
}
