package com.nowcoder.community.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.util.RedisKeyUtil;
import com.nowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private RedisTemplate redisTemplate;

    //最多支持15页帖子，单位是页
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    //本地缓存10S自动清理
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口：Cache,LoadingCache,AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                // 当尝试从缓存中读取数据时，有则返回。没有数据则需要去另一位置查询到数据，即此方法
                .build(new CacheLoader<String,List<DiscussPost>>(){
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key == null |key.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        String redisKey = RedisKeyUtil.getHotPostKey();
                        if(redisTemplate.opsForHash().hasKey(redisKey,key)){
                            String json = (String)redisTemplate.opsForHash().get(redisKey,key);
                            List<DiscussPost> value = JSONObject.parseArray(json, DiscussPost.class);
                            return value;
                        }

                        String[] params = key.split(":");
                        if(params == null || params.length != 2){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 查找需要缓存的数据 userId和orderMode固定
                        logger.debug("load post list from DB.");
                        List<DiscussPost> postList = discussPostMapper.selectDiscussPosts(0,offset,limit,1);

                        //二级缓存：分布式缓存
                        String value=JSON.toJSON(postList).toString();
                        redisTemplate.opsForHash().put(redisKey,key,value);

                        return postList;
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer,Integer>(){
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        // 二级缓存：Redis -> mysql (待做)

                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });

    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode){
        // 对热门排序进行缓存
        if(userId == 0 && orderMode == 1){
            return postListCache.get(offset + ":" +limit);
        }

        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
    }

    public int findDiscussPostRows(int userId){
        // 对非用户帖子数进行缓存
        if(userId == 0){
            // userId永远为0
            return postRowsCache.get(userId);
        }

        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post) throws IllegalAccessException {
        if(post == null){
            throw new IllegalAccessException("参数不能为空！");
        }

        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id,int type){
        return discussPostMapper.updateType(id,type);
    }

    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }

    public int insertTestData(List<DiscussPost> list){
        return discussPostMapper.saveSheet(list);
    }
}
