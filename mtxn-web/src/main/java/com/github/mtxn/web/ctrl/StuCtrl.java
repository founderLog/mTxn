package com.github.mtxn.web.ctrl;

import com.github.mtxn.web.service.StuService;
import com.github.mtxn.web.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/stu")
public class StuCtrl {

    @Autowired
    private StuService stuService;

    @GetMapping("/list")
    public List<Student> list(){
        List<Student> students = stuService.list();
        return students;
    }


    @GetMapping("/save")
    public String save(String city,String name,String school){
        stuService.save(city, name, school);
        return "success";
    }
}
