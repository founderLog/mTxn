package com.github.mtxn.web.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsAutoIncrement;
import com.gitee.sunchenbin.mybatis.actable.annotation.IsKey;
import com.gitee.sunchenbin.mybatis.actable.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "com_student",comment="学生信息表") //对应表名
public class Student implements Serializable {

    //对应id，可不填
    @TableId(type = IdType.AUTO)//mybatis-plus主键注解
    @IsAutoIncrement   //自增
    @IsKey             //actable主键注解
    @Column(comment = "学生ID")//对应数据库字段，不配置name会直接采用属性名作为字段名comment是注解
    private Integer id;

    @Column(comment = "名字")
    private String name;

    @Column(comment = "学校")
    private String school;

    @Column(comment = "城市")
    private String city;

    @Column(comment = "地址")
    private String address;
}