package com.github.mtxn.web.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "com_student") //对应表名
public class Student implements Serializable {

    //对应id，可不填
    @TableId(type = IdType.AUTO)//mybatis-plus主键注解
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("school")
    private String school;

    @TableField("city")
    private String city;

    @TableField("address")
    private String address;
}