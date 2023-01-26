package com.example.board.api.comment.service;

import com.example.board.api.comment.entity.Comment;
import com.example.board.api.comment.repository.CommentRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    // 해당 post_id 에 댓글의 갯수
    public int CommentCntByPostId(long post_id) {
        return commentRepository.commentCntByPostId(post_id);
    }

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public String saveComment(Long postId, Comment comment) {
        return commentRepository.saveComment(postId, comment);
    }

    public String updateComment(Long postId, Long id, Comment comment) {
        return commentRepository.updateComment(id, comment);
    }

    public int deleteComment(Long postId, long id) {
        return commentRepository.deleteComment(postId, id);
    }
}
