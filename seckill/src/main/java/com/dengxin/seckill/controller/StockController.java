package com.dengxin.seckill.controller;


import com.dengxin.seckill.service.OrderService;
import com.dengxin.seckill.service.UserService;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("stock")
public class StockController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    //创建令牌桶,桶内最大令牌数为10
    private RateLimiter rateLimiter = RateLimiter.create(10);

//乐观锁解决超卖+令牌桶限流+redis设置商品秒杀过期时间+md5加密实现接口隐藏+单用户频率访问限制
    @RequestMapping("kill")
    public String kill(Integer id,Integer userId,String md5) {
        System.out.println("商品的id为" + id);

        //两秒后还未获取到令牌就请求失败
        if(!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)){
            System.out.println("商品抢购太过火爆请稍后重试");
            return "商品抢购太过火爆请稍后重试";
        }
        try {
            //当前用户调用次数限制
            //查询当前用户调用次数
            int count = userService.saveUserCount(userId);
            System.out.println("当前用户访问的次数为:"+count);
            boolean userCount = userService.getUserCount(userId);
            if (userCount){
                System.out.println("用户访问次数超过限制,抢购失败");
                return "您的访问次数过多,请稍后重试";
            }
            //根据id进行秒杀
            int orderId = orderService.kill(id,userId,md5);
            System.out.println("秒杀成功，订单id:" + orderId);
            return "秒杀成功，订单id:" + orderId;

        } catch (Exception e) {
            e.printStackTrace();
            return "订单秒杀失败" + e.getMessage();
        }

    }



    //生成md5值的方法
    @RequestMapping("md5")
    public String getMd5(Integer id, Integer userId) {
        String md5;
        try {
            md5 = orderService.getMd5(id, userId);
        }catch (Exception e){
            e.printStackTrace();
            return "获取md5失败: "+e.getMessage();
        }
        return "该用户的md5信息为: "+md5;
    }
//乐观锁+令牌桶，防止超卖和接口限流
//    @RequestMapping("kill")
//    public String kill(Integer id) {
//        System.out.println("商品的id为" + id);
//
//        //两秒后还未获取到令牌就请求失败
//        if(!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)){
//            System.out.println("商品抢购太过火爆请稍后重试");
//            return "商品抢购太过火爆请稍后重试";
//        }
//        try {
//            //根据id进行秒杀
//                int orderId = orderService.kill(id);
//                System.out.println("秒杀成功，订单id:" + orderId);
//                return "秒杀成功，订单id:" + orderId;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "订单秒杀失败" + e.getMessage();
//        }
//
//    }




    //悲观锁实现
//    @RequestMapping("kill")
//    public String kill(Integer id) {
//        System.out.println("商品的id为" + id);
//        try {
//            //根据id进行秒杀
//            synchronized (this){
//                int orderid = orderService.kill(id);
//                return "秒杀成功，订单id:" + orderid;
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "订单秒杀失败" + e.getMessage();
//        }
//    }

//乐观锁实现
//    @RequestMapping("kill")
//    public String kill(Integer id) {
//        System.out.println("商品的id为" + id);
//        try {
//            //根据id进行秒杀
//                int orderid = orderService.kill(id);
//                return "秒杀成功，订单id:" + orderid;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "订单秒杀失败" + e.getMessage();
//        }
//
//    }
}
