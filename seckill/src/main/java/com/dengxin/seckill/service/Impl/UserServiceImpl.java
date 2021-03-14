package com.dengxin.seckill.service.Impl;

import com.dengxin.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public int saveUserCount(Integer userId) {
        //根据不同用户id生成调用次数的key
        String countKey = "COUNT_LOGIN" + "_" + userId;
        //获取redis中指定key的调用次数
        String countNum = stringRedisTemplate.opsForValue().get(countKey);
        int count =-1;
        if (countNum == null) {
            //第一次调用放入redis中设置为0
            stringRedisTemplate.opsForValue().set(countKey, "0", 360, TimeUnit.SECONDS);
        } else {
            //不是第一次调用每次+1
            count = Integer.parseInt(countNum) + 1;
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(count), 3600, TimeUnit.SECONDS);
        }
        return count;//返回调用次数
    }

    @Override
    public boolean getUserCount(Integer userId) {
        String countKey = "COUNT_LOGIN"+ "_" + userId;
        //跟库用户调用次数的key获取redis中调用次数
        String countNum = stringRedisTemplate.opsForValue().get(countKey);
        if (countNum == null) {
            //为空直接抛弃说明key出现异常
            System.out.println("该用户无访问签名,可能过期或错误");
            return true;
        }
        return Integer.parseInt(countNum) > 10; //false代表没有超过 true代表超过
    }

}
