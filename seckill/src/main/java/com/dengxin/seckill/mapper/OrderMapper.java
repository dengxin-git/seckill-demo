package com.dengxin.seckill.mapper;

import com.dengxin.seckill.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {

    //创建订单
    void createOrder(Order order);
}
