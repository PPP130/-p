package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    @Select("select * from address_book where user_id = #{userId} order by is_default desc")
    List<AddressBook> listByUserId(Long userId);

    @Select("select * from address_book where id = #{id}")
    AddressBook getById(Long id);

    @Insert("insert into address_book(user_id, consignee, phone, sex, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) " +
            "values(#{userId}, #{consignee}, #{phone}, #{sex}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}, #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AddressBook addressBook);

    @Update("update address_book set consignee=#{consignee}, phone=#{phone}, sex=#{sex}, province_code=#{provinceCode}, province_name=#{provinceName}, " +
            "city_code=#{cityCode}, city_name=#{cityName}, district_code=#{districtCode}, district_name=#{districtName}, detail=#{detail}, label=#{label} where id=#{id}")
    void update(AddressBook addressBook);

    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void updateIsDefaultToZero(Long userId);

    @Update("update address_book set is_default = 1 where id = #{id}")
    void updateIsDefaultToOne(Long id);

    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);
}
