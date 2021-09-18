package com.spdbccc.controller;

import com.spdbccc.task.ComPressTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Date 2021/9/17
 * @Author xujin
 * @TODO
 * @Version 1.0
 */
@RestController
public class CompressController {

    @Autowired
    private ComPressTask comPressTask;

    @RequestMapping("/test")
    public String test() {
        comPressTask.parseFile();
        return "compress success!";
    }
}
