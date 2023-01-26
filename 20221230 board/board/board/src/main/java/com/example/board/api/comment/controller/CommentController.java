package com.example.board.api.comment.controller;

import com.example.board.api.comment.entity.Comment;
import com.example.board.api.comment.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // 댓글 달기
    @PostMapping("/{postId}/comment/new")
    public String saveComment(
//       PathVariable : url에서 각 구분자에 들어오는 값을 처리해야 할 때, index/1 이런식
            @PathVariable Long postId,
            Comment comment
    ) {
        log.debug("/comment/new start");
        commentService.saveComment(postId, comment);
        return "redirect:/view/"+postId;
    }

    // 댓글 삭제
    @PostMapping("/{postId}/comment/{id}/delete")
    public String deleteComment(
//         PathVariable : url에서 각 구분자에 들어오는 값을 처리해야 할 때, index/1 이런식
            @PathVariable Long postId,
            @PathVariable long id
    ) {
        log.debug("/comment/delete start");
        commentService.deleteComment(postId, id);
        return "";
    }
}
