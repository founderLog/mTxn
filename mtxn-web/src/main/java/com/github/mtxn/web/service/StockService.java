package com.github.mtxn.web.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.mtxn.transaction.annotation.MultiTransaction;
import com.github.mtxn.web.entity.Stock;
import com.github.mtxn.web.mapper.StockMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
@AllArgsConstructor
public class StockService {
    private final StockMapper stockMapper;

    public List<Stock> list() {
        return stockMapper.selectList(Wrappers.query());
    }

    /**
     * 往库存表增加一个库存记录
     * @param dataSourceId
     * @param stock
     * @return
     */
    @MultiTransaction(datasourceId = "#dataSourceId")
    public Stock save(Integer dataSourceId,Stock stock) {
        stockMapper.insert(stock);
        return stock;
    }

    public void update(Stock stock) {
        stockMapper.updateById(stock);
    }

    @MultiTransaction(datasourceId = "#dataSourceId")
    public void decreasingStock(Integer dataSourceId,long amount,Integer id){
        Stock stock = stockMapper.selectById(id);
        stock.setAmount(stock.getAmount() - amount);
        stock.setLastModifyTime(new Timestamp(System.currentTimeMillis()));
        stockMapper.updateById(stock);
    }
}
