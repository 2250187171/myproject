package org.java.web;

import org.java.service.A_AuthorityService;
import org.java.service.A_RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthorityController {

    @Autowired
    private A_RoleService roleServic;
    @Autowired
    private A_AuthorityService authorityService;
    //进入权限管理页面
    @RequestMapping("/userManage/authorityShow")
    public String authorityShow(Model model){
        //获得所有角色
        List<Map> roles = roleServic.findRoleAll();
        model.addAttribute("roles", roles);
        return "/userManage/authorityManage";
    }

    //获得所有权限
    @RequestMapping("/userManage/authorityAll")
    @ResponseBody
    public Map findAll(){
        Map m=new HashMap();
        m.put("code", 0);
        m.put("msg", "ok");
        m.put("data", authorityService.findAll());
        return m;
    }
}