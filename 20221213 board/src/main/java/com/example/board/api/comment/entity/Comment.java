package com.example.board.api.comment.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {

    private long id;

    private String name;

    private String body;

    private String email;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
