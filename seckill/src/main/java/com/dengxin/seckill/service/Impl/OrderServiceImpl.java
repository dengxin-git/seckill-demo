package com.dengxin.seckill.service.Impl;

import com.dengxin.seckill.entity.Order;
import com.dengxin.seckill.entity.Stock;
import com.dengxin.seckill.entity.User;
import com.dengxin.seckill.mapper.OrderMapper;
import com.dengxin.seckill.mapper.StockMapper;
import com.dengxin.seckill.mapper.UserMapper;
import com.dengxin.seckill.service.OrderService;
import com.dengxin.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
@Transactional
public class OrderServiceImpl  implements OrderService {

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    @Override
    public  int kill(Integer id,Integer userId,String md5) {

        if(!stringRedisTemplate.hasKey("seckill"+id)){
            throw new RuntimeException("无该秒杀商品或者秒杀活动已结束");
        }

        //验证签名
        String hashKey = "KEY_"+userId+"_"+id;
        String userKey = stringRedisTemplate.opsForValue().get(hashKey);
        if(userKey==null){
            throw new RuntimeException("用户签名为空,验证签名错误");
        }
        if(!userKey.equals(md5)){
            throw new RuntimeException("用户签名错误");
        }

        //根据id校验库存
        Stock stock = checkStoock(id);
        //扣除库存
        updateStock(stock);
        //创建订单
        return createOrder(stock);
    }

    @Override
    public String getMd5(Integer id, Integer userid) {
        //检验用户的合法性
        User user = userMapper.findById(userid);
        if(user==null)throw new RuntimeException("用户信息不存在!");
        System.out.println(("用户信息:"+user));
        //检验商品的合法行
        Stock stock = stockMapper.checkStock(id);
        if(stock==null) throw new RuntimeException("商品信息不合法!");
        System.out.println("商品信息:"+stock);
        //生成hashkey
        String hashKey = "KEY_"+userid+"_"+id;
        //生成md5//这里!QS#是一个盐 随机生成
        String key = DigestUtils.md5DigestAsHex((userid+id+"1a2b3c").getBytes());
        stringRedisTemplate.opsForValue().set(hashKey, key, 360, TimeUnit.SECONDS);
        System.out.println("向redis set MD5"+hashKey+key);
        return key;

    }

    //校验库存
    private Stock checkStoock(Integer id){
        Stock stock = stockMapper.checkStock(id);
        if(stock.getSale().equals(stock.getCount())||stock.getSale()>stock.getCount()){
            throw new RuntimeException("库存不足");
        }
        return stock;
    }
    //扣除库存
    private void updateStock(Stock stock){
        int updateNum = stockMapper.updateSale(stock);
        if(updateNum<=0){
            throw new RuntimeException("秒沙失败");
        }
    }
    //创建订单
    private Integer createOrder(Stock stock){
        Order order = new Order();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        order.setCreateDate(new Date());
        orderMapper.createOrder(order);

        return order.getId();
    }
}
