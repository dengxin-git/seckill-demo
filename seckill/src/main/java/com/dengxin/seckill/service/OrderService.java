package com.dengxin.seckill.service;

public interface OrderService {
    int kill(Integer id,Integer userId,String md5);

    String getMd5(Integer id, Integer userid);
}
