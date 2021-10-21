package com.nowcoder.community.controller;

import com.nowcoder.community.Event.EventProducer;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();

        //登录后才能访问 (待做)
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType) // entityType 3
                .setEntityId(entityId) // 则为关注目标用户id
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已关注！" );
    }

    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();

        //登录后才能访问 (待做)

        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消！" );
    }

    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int)followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        // 关注目标列表
        List<Map<String,Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if(userList !=null){
            for(Map<String,Object> map:userList){
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));

            }
        }
        model.addAttribute("users",userList);

        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId")int userId, Page page, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER,userId));
        // 关注目标列表
        List<Map<String,Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(userList !=null){
            for(Map<String,Object> map:userList){
                User u = (User)map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));

            }
        }
        model.addAttribute("users",userList);

        return "/site/follower";
    }

    // 判断是否有关注:关注列表中某用户的关注是否有 登录用户
    private boolean hasFollowed(int userId){
        // 没登录则没关注
        if(hostHolder.getUser() == null){
            return false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }
}
