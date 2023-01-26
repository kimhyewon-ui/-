package com.example.board.api.comment.controller;

import com.example.board.api.comment.entity.Comment;
import com.example.board.api.comment.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/board")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 달기
    @PostMapping("/{postId}/comment/new")
    public String saveComment(
            @PathVariable Long postId,
            @RequestBody Comment comment
    ) {
        log.debug("/comment/new start");
        commentService.saveComment(postId, comment);
        return "";
    }

    // 댓글 수정
    @PostMapping("/{postId}/comment/{id}/update")
    public String updateComment(
            @PathVariable Long postId,
            @PathVariable Long id,
            @RequestBody Comment comment
    ) {
        log.debug("/comment/update start");
        commentService.updateComment(postId, id, comment);
        return "";
    }
    // 댓글 삭제
    @PostMapping("/{postId}/comment/{id}/delete")
    public String deleteComment(
            @PathVariable Long postId,
            @PathVariable long id
    ) {
        log.debug("/comment/delete start");
        commentService.deleteComment(postId, id);
        return "";
    }
}
