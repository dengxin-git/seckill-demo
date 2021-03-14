package com.dengxin.seckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Order {

    private Integer id;
    private Integer sid;
    private String name;
    private Date createDate;
}
