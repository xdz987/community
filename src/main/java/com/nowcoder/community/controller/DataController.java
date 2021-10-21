package com.nowcoder.community.controller;

import com.nowcoder.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    // 统计页面
    @RequestMapping(path = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

    // 统计网站UV
    @RequestMapping(path = "/data/uv",method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                        Model model) throws IllegalAccessException {
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult",uv);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);

        // 转发到另一平级方法
        return "forward:/data";
//        return "/site/admin/data";
    }

    // 统计活跃用户
    @RequestMapping(path = "/data/dau",method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end,
                        Model model) throws IllegalAccessException {
        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult",dau);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);

        // 转发到另一平级方法
        return "forward:/data";
//        return "/site/admin/data";
    }
}
