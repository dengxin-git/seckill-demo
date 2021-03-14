package com.dengxin.seckill.mapper;

import com.dengxin.seckill.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockMapper {

    //根据商品id查询库存
    public Stock checkStock(Integer id);

    //根据商品id更新库存
    int updateSale(Stock stock);
}
