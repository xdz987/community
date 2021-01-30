package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "Hello,world!";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData(){
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while(enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name+": "+value);
        }
        System.out.println(request.getParameter("code"));

        // 返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try(PrintWriter writer = response.getWriter();) {
            writer.write("<h1>牛客网</h1>");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 1. 参数放于url，限定GET请求
    // /students?current=1&limit=20
    //限制了只能使用Get方法访问该路由
    @RequestMapping(path = "/students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            //@RequestParam注解：设定默认值
            @RequestParam(name="current",required = false,defaultValue = "1") int current,
            @RequestParam(name="limit",required = false,defaultValue = "10") int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    // 2. 通过路径获取参数，限定GET请求
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(
            // 从路径中获取同名变量
            @PathVariable("id") int id
    ){
        System.out.println(id);
        return "a student";
    }

    // 3. 限定POST请求
    // 形参的名字与请求的param一样即可，无需注解
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    // 4. 响应HTML数据
    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    // (1)不加@ResponseBody默认为html页面
    // (2)返回Model和View给模板引擎渲染成HTML
    public ModelAndView getTeacher(){
        ModelAndView mav = new ModelAndView();
        // (3)往模板里存放(数据)对象
        mav.addObject("name","张三");
        mav.addObject("age",30);
        // (4)设置模板的路径和名字，templates路径不用写
        mav.setViewName("/demo/view");
        return mav;
    }

    // 5. 返回view的路径而不是模板
    @RequestMapping(path = "/school",method = RequestMethod.GET)
    // 形参的Model对象，是DispatcherServlet调用该方法时自动实例化(Bean)传递过来
    public String getSchool(Model model){
        model.addAttribute("name","清华大学");
        model.addAttribute("age",15);
        return "/demo/view";
    }

    // 6. 相应JSON数据字符串(异步请求)
    // Java对象 <->JSON字符串 <->JS对象
    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody
    // 声明返回的是 Map<String,Object>又加了@ResponseBody，则自动转换为Json字符串
    public Map<String,Object> getEmp(){
        Map<String,Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", 23);
        emp.put("salary", 30000.00);
        return emp;
    }

    // 7. 相应JSON数据对象
    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    @ResponseBody
    // 声明返回的是 Map<String,Object>又加了@ResponseBody，则自动转换为Json对象
    public List<Map<String,Object>> getEmps(){
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> emp = new HashMap<>();
        emp.put("name", "张三");
        emp.put("age", 23);
        emp.put("salary", 30000.00);
        list.add(emp);

        emp.put("name", "李四");
        emp.put("age", 25);
        emp.put("salary", 50000.00);
        list.add(emp);

        emp.put("name", "王五");
        emp.put("age", 27);
        emp.put("salary", 80000.00);
        list.add(emp);

        return list;
    }
}