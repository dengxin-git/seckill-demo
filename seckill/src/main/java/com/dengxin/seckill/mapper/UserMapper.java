package com.dengxin.seckill.mapper;

import com.dengxin.seckill.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    public User findById(Integer id);
}
