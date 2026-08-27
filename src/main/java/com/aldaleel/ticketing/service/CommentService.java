package com.aldaleel.ticketing.service;

import com.aldaleel.ticketing.entity.Comment;
import com.aldaleel.ticketing.entity.Ticket;
import com.aldaleel.ticketing.entity.User;
import com.aldaleel.ticketing.repository.CommentRepository;
import com.aldaleel.ticketing.repository.TicketRepository;
import com.aldaleel.ticketing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository,
            TicketRepository ticketRepository,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public Comment createComment(
            UUID ticketId,
            UUID authorId,
            String content
    ) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (ticket.getStatus() == Ticket.Status.CLOSED) {
            throw new IllegalStateException(
                    "Cannot add comments to a closed ticket"
            );
        }

        Comment comment = Comment.builder()
                .content(content)
                .ticket(ticket)
                .author(author)
                .build();

        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByTicket(UUID ticketId) {

        if (!ticketRepository.existsById(ticketId)) {
            throw new IllegalArgumentException("Ticket not found");
        }

        return commentRepository.findByTicketId(ticketId);
    }

    @Transactional(readOnly = true)
    public Comment getCommentById(UUID id) {

        return commentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Comment not found")
                );
    }

    public void deleteComment(UUID id) {

        Comment comment = getCommentById(id);

        commentRepository.delete(comment);
    }
}