package com.baidu.controller;

import com.baidu.entity.Student;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

/**
 * @ClassName TestController
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/15
 * @Version V1.0
 **/
@Controller
public class TestController {

    @GetMapping("test")//定义url使用model map返回数据
    public String test(ModelMap map){
        map.put("name","tomcat");
        return "test";
    }

    @GetMapping("student")
    public String student(ModelMap map){
        Student student = new Student();
        student.setCode("001");
        student.setPass("010101");
        student.setAge(20);
        student.setLikeColor("<font color='red'>黑色</font>");

        map.put("stu",student);

        return "test";
    }

    //循环
    @GetMapping("list")
    public String list(ModelMap map){
        Student s1 = new Student("01", "1223", 18, "red");
        Student s2 = new Student("02", "3323", 17, "blue");
        Student s3 = new Student("03", "4543", 61, "red");
        Student s4 = new Student("04", "5657", 28, "blue");
        Student s5 = new Student("05", "5657", 68, "blue");
        //转为list
        map.put("stuList", Arrays.asList(s1,s2,s3,s4,s5));
        return "list";
    }

}
