package org.java.web;

import org.java.service.A_RoleService;
import org.java.service.A_SectionService;
import org.java.service.A_UserService;
import org.java.util.IdentificationCard;
import org.java.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {
    @Autowired
    private A_UserService service;

    @Autowired
    private IdentificationCard card;

    @Autowired
    private A_RoleService roleService;

    @Autowired
    private A_SectionService sectionService;

    @Autowired
    private MD5 md5;
    //跳转进入用户信息页面
    @RequestMapping("/userManage/userMessage")
    public String userMessage(Model model){
        //获得所有角色
        model.addAttribute("roles", roleService.findRoleAll());
        //获得所有部门
        model.addAttribute("sections", sectionService.findSectionAll());
        return "/userManage/userMessage";
    }

    //跳转进入主页
    @RequestMapping("/tomain")
    public String main(Model model){
        return "/main";
    }

    //新增用户返回json
    @RequestMapping("userManage/addUser")
    @ResponseBody
    public String addUser(@RequestParam Map map, HttpServletRequest request) throws Exception {
        if(service.findByPassword(map.get("phoneNumber").toString())!=null){
            return "该手机号已被注册";
        }else if(service.findByIDNumber(map.get("IDNumber").toString())!=0){
            return "该身份证已被注册";
        }
        //获得项目根路径
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath()+"static/images";
        //获得文件上传的请求
        MultipartHttpServletRequest re= (MultipartHttpServletRequest) request;
        //获得所有名称为Myfile的文件
        List<MultipartFile> list = re.getFiles("picture");
        if(list.isEmpty()){//判断是否为空
            return "文件上传失败";
        }
        //将文件转换成字节数组
        byte[] IDNumberpath= list.get(1).getBytes();

        //解析身份证图片获得图片的身份证信息
        Map IDNumber = card.idCard(IDNumberpath);

        //获得部门id
        int sectionID=Integer.parseInt(map.get("sectionID").toString());
        //获得角色ID
        int roleID=Integer.parseInt(map.get("roleID").toString());
        System.out.println(map);
        System.out.println(IDNumber.get("name")+"-----"+map.get("username"));
        //判断身份证
        if(!IDNumber.get("ID").equals(map.get("IDNumber"))){
            return "身份证号码和输入号码不一致";
        }else if(!IDNumber.get("name").equals(map.get("username"))){
            return "身份证姓名和输入姓名不一致";
        }else if(sectionID==2&&roleID==1){//判断该用户是不是司机
            byte[] drivingLicence=list.get(3).getBytes();
            //解析驾驶证图片获得驾驶证信息
            Map Licence=card.drivingLicence(drivingLicence);
            SimpleDateFormat format=new SimpleDateFormat("yyyyMMdd");
            //获得当前时间的时间戳
            long time = new Date().getTime();
            //获得驾驶证到期时间的时间戳
            long time1 = format.parse(Licence.get("endDate").toString()).getTime();
            //获得当前时间
            if(!map.get("username").equals(Licence.get("name"))){
                return "驾驶证姓名和输入姓名不一致";
            }else if(time1<time){
                return "您的驾驶证已过期";
            }
        }
        for (MultipartFile f:list) {
            //获得文件名称
            String fileName= map.get("phoneNumber")+f.getOriginalFilename();

            //创建文件
            File newfile=new File(path, fileName);
            //判断文件的父级目录是否存在，不存在创建
            if(!newfile.getParentFile().exists()){
                newfile.getParentFile().mkdirs();
            }
            f.transferTo(newfile);
        }
        //对密码进行加密
        Object loginPassword = md5.passwordMD5(map.get("loginPassword").toString());
        Object paymentpassword = md5.passwordMD5(map.get("paymentpassword").toString());
        map.put("loginPassword",loginPassword.toString());
        map.put("paymentpassword", paymentpassword.toString());
        //新增用户
        service.addUser(map);
        return "1";
    }

    @RequestMapping("/userManage/findUserAll")
    @ResponseBody
    public Map findUserAll(@RequestParam Map map,int start,int length){
        map.put("start", start);
        map.put("length", length);
        List<Map> userAll = service.findUserPaging(map);
        Map m=new HashMap();
        m.put("draw",map.get("draw"));
        m.put("recordsTotal", service.count());
        m.put("recordsFiltered", service.count());
        m.put("aaData", userAll);
        return m;
    }
}
