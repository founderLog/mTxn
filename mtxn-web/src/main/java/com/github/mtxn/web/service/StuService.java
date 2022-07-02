package com.github.mtxn.web.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.mtxn.web.mapper.StudentMapper;
import com.github.mtxn.web.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StuService {
    @Autowired
    private StudentMapper studentMapper;

    public List<Student> list(){
        List<Student> students = studentMapper.selectList(Wrappers.<Student>lambdaQuery().select(Student::getName));
        return students;
    }


    @Transactional
    public void save(String city,String name,String school){
        Student student = new Student();
        student.setCity(city);
        student.setName(name);
        student.setSchool(school);
        studentMapper.insert(student);
    }
}
