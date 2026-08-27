package com.aldaleel.ticketing.mapper;

import com.aldaleel.ticketing.dto.response.CommentResponse;
import com.aldaleel.ticketing.entity.Comment;

public class CommentMapper {

    private CommentMapper() {
    }

    public static CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor() != null
                        ? comment.getAuthor().getId()
                        : null,
                comment.getAuthor() != null
                        ? comment.getAuthor().getName()
                        : null,
                comment.getCreatedAt()
        );
    }
}