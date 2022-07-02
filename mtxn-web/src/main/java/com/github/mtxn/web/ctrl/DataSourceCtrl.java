package com.github.mtxn.web.ctrl;

import com.github.mtxn.entity.DataSource;
import com.github.mtxn.service.DataSourceService;
import com.github.mtxn.web.entity.Student;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/dataSource")
@AllArgsConstructor
public class DataSourceCtrl {
    private DataSourceService dataSourceService;

    @GetMapping("/list")
    @ResponseBody
    public List<DataSource> list() {
        List<DataSource> dataSourceList = dataSourceService.getAll();
        return dataSourceList;
    }
}
