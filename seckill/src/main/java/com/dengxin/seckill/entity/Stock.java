package com.dengxin.seckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Stock implements Serializable {

    private Integer id;
    private String name;
    private Integer count;
    private Integer sale;
    private Integer version;
}
