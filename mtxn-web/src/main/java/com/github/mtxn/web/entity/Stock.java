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
@TableName(value = "com_stock") //对应表名
public class Stock implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("amount")
    private Long amount;

    @TableField("lastModifyTime")
    private Timestamp lastModifyTime;

}