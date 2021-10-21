package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.*;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private FollowService followService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(Model model){
        // 上传文件名称 使用uuid原因：1.历史头像功能 2.缓存
        String fileName = CommunityUtil.generateUUID();

        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));

        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return "/site/setting";
    }

    // 更新头像路径
    @RequestMapping(path = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空！");
        }

        String url = headerBucketUrl +"/" +fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    // 废弃
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }

        // 上传文件
        String fileName = headerImage.getOriginalFilename();
        // 截取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确！");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath+"/"+fileName);

        try {
            // 将当前文件写到目录下
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！",e);
        }

        // 更新当前用户头像路径(web访问路径)
        // http://localhost:8080/community/userheader/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return  "redirect:/index";
    }

    // 废弃
    //既不是网页也不是字符串，所以直接往response写入一个流，返回类型直接void
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/"+ suffix);
        try (
                // response的输出流，MVC会自动关闭该流
                OutputStream os = response.getOutputStream();
                // 获取图片的输入流（即读图片数据）
                FileInputStream fis = new FileInputStream(fileName);
                ){
            //建立一个缓冲区
            byte[] buffer = new byte[1024];
            // 游标
            int b = 0;
            // 每次最多读到1024KB数据，读不到则为-1
            while((b = fis.read(buffer)) != -1){
                // 写到response的输出流
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败："+e.getMessage());
        }
    }

    @RequestMapping(path = "/password", method = RequestMethod.POST)
    public String updatePassword(@CookieValue("ticket") String ticket,String oldPassword, String newPassword, Model model,User user){
        user = hostHolder.getUser();
        Map<String,Object> map = userService.updatePassword(user.getId(), oldPassword,newPassword);
        if(map == null || map.isEmpty()){
            userService.logout(ticket);
            // 重定向默认get请求
            return "redirect:/login";
        }else{
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            return "/site/setting";
        }
    }

    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user",user);
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser() !=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }

    @RequestMapping(path = "/mypost/{userId}",method = RequestMethod.GET)
    public String getMyPostPage(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        // 用户
        model.addAttribute("user",user);

        int postRows = discussPostService.findDiscussPostRows(userId);
        page.setRows(postRows);
        List<DiscussPost> list = discussPostService.findDiscussPosts(userId,page.getOffset(),page.getLimit(),0);

        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list!=null){
            for (DiscussPost post:list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",post);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("postRows",postRows);

        return "/site/my-post";
    }

    @RequestMapping(path = "/myreply/{userId}",method = RequestMethod.GET)
    public String getMyReplyPage(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }
        // 用户
        model.addAttribute("user", user);

        int commentRows = commentService.findCountByUser(userId,1);
        page.setRows(commentRows);
        List<Comment> list = commentService.findCommentsByUser(userId,1,page.getOffset(),page.getLimit());

        List<Map<String,Object>> comments = new ArrayList<>();

        if(list!=null){
            for(Comment comment:list){
                Map<String,Object> map = new HashMap<>();
                map.put("comment",comment);
                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
                map.put("post",post);
                comments.add(map);
            }
        }

        model.addAttribute("comments",comments);
        model.addAttribute("commentRows",commentRows);

        return "/site/my-reply";
    }
}
