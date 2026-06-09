package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 新增用户
     */
    @Insert("insert into user (openid, name, avatar, create_time) values (#{openid}, #{name}, #{avatar}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    @Select("select count(id) from user where create_time between #{begin} and #{end}")
    Integer countByCreateTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    @Select("select count(id) from user")
    Integer count();

    @Select("select * from user where id = #{id}")
    User getById(Long id);
}
