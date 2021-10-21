package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public List<Comment> findCommentsByUser(int userId,int entityType,int offset,int limit){
        return commentMapper.selectCommentsByUser(userId,entityType,offset,limit);
    }

    public int findCommentCount(int entityType,int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    public int findCountByUser(int userId,int entityType){
        return commentMapper.selectCountByUser(userId,entityType);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) throws IllegalAccessException{
        if(comment == null){
            throw new IllegalAccessException("参数不能为空！");
        }

        // 转义+敏感词过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        // 添加评论
        int rows = commentMapper.insertComment(comment);
        System.out.println(comment);
        System.out.println(rows);

        // 更新帖子的评论数量(回复不计算)
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
}
