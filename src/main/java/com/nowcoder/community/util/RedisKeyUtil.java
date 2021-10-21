package com.nowcoder.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    // 点赞实体
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    // 点赞的用户
    private static final String PREFIX_USER_LIKE = "like:user";
    // 关注目标
    private static final String PREFIX_FOLLOWEE = "followee";
    // 被关注者(粉丝)
    private static final String PREFIX_FOLLOWER = "follower";
    // 验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";
    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";
    // 用户信息
    private static final String PREFIX_USER = "user";
    // 独立访客
    private static final String PREFIX_UV = "uv";
    // 日活跃用户
    private static final String PREFIX_DAU = "dau";
    // 帖子
    private static final String PREFIX_POST = "post";


    // 某个实体的赞
    // 谁个某个实体(帖子、评论)点赞，就将该用户的id存入该实体的集合中。这样可以满足各种业务需求的变化
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:entityId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体(用户 帖子等)，根据时间排序可满足按时间顺序查找等业务需求
    // followee:userId:entityType: -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entity:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码 用户还没登录，但需要识别用户是谁：
    // 给浏览器客户端，发送一个临时的登录凭证，存到cookie中
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

    public static String getHotPostKey() {
        return PREFIX_POST + SPLIT + "hot";
    }
}