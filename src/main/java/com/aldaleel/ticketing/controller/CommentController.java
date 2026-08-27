package com.aldaleel.ticketing.controller;

import com.aldaleel.ticketing.dto.request.CreateCommentRequest;
import com.aldaleel.ticketing.dto.response.CommentResponse;
import com.aldaleel.ticketing.entity.Comment;
import com.aldaleel.ticketing.mapper.CommentMapper;
import com.aldaleel.ticketing.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        Comment comment = commentService.createComment(
                ticketId,
                request.authorId(),
                request.content()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommentMapper.toResponse(comment));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID ticketId
    ) {
        List<CommentResponse> response = commentService
                .getCommentsByTicket(ticketId)
                .stream()
                .map(CommentMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable UUID ticketId,
            @PathVariable UUID commentId
    ) {
        Comment comment = commentService.getCommentById(commentId);

        return ResponseEntity.ok(
                CommentMapper.toResponse(comment)
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID ticketId,
            @PathVariable UUID commentId
    ) {
        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }
}