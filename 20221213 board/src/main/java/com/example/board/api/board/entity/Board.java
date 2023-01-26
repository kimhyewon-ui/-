package com.example.board.api.board.entity;

import com.example.board.api.comment.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Board {

    private long id;
    private String title;
    private String content;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int commentCnt;
    private List<Comment> comments = new ArrayList<>();


}
