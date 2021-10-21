package com.nowcoder.community.quartz;

import com.alibaba.fastjson.JSON;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostHotRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostHotRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    //最多支持10页帖子，单位是页
    @Value("${redis.posts.max-size}")
    private int maxSize;

    //redis缓存8S自动清理
    @Value("${redis.posts.expire-seconds}")
    private int expireSeconds;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            for(int i=0;i<maxSize;i++){
                refresh(i,10);
            }
        }catch (Exception e){
            logger.info("[redis缓存] 更新失败！");
        }
        logger.info("[redis缓存] 完成更新！");
    }

    private void refresh(int offset, int limit) {
        String redisKey = RedisKeyUtil.getHotPostKey();
        List<DiscussPost> postList = discussPostMapper.selectDiscussPosts(0,offset,limit,1);

        String value= JSON.toJSON(postList).toString();
        redisTemplate.opsForHash().put(redisKey,offset + ":" +limit,value);
    }
}
