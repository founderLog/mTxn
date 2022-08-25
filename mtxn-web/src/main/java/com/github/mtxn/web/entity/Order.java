package com.github.mtxn.web.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "com_order") //对应表名
public class Order implements Serializable {

    //对应id，可不填
    @TableId(type = IdType.AUTO)//mybatis-plus主键注解
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("orderNo")
    private String orderNo;

    @TableField("createTime")
    private Timestamp createTime;

    @TableField("address")
    private String address;
}