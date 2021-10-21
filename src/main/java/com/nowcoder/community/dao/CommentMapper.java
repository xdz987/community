package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);

    int selectCountByEntity(int entityType,int entityId);

    int selectCountByUser(int userId,int entityType);

    List<Comment> selectCommentsByUser(int userId,int entityType,int offset,int limit);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);
}
