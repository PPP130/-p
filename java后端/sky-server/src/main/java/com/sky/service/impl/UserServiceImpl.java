package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录（含自动注册）
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 1. 调用微信 code2Session 接口获取 openid
        String openid = getOpenid(userLoginDTO.getCode());

        // 2. 根据 openid 查询用户
        User user = userMapper.getByOpenid(openid);

        // 3. 用户不存在则自动注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .name("微信用户")
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

    /**
     * 调用微信 code2Session 接口获取 openid
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        // 构建请求参数
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type", "authorization_code");

        // 调用微信接口
        String result = HttpClientUtil.doGet(WX_LOGIN_URL, paramMap);

        // 解析返回结果
        JSONObject jsonObject = JSON.parseObject(result);
        String openid = jsonObject.getString("openid");

        // 校验 openid 是否获取成功
        if (openid == null || openid.isEmpty()) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        return openid;
    }
}
