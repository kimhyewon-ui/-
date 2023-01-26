package com.example.board.api.comment.repository;

import com.example.board.api.comment.entity.Comment;
import com.example.board.core.utils.LocalDateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class CommentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int commentCntByPostId(long post_id) {
        return jdbcTemplate.queryForObject("select count(*) from comments where post_id = ?", Integer.class, post_id);
    }

    public String saveComment(Long postId, Comment comment) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("comments").usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", comment.getName());
        parameters.put("body", comment.getBody());
        parameters.put("email", comment.getEmail());
        parameters.put("post_id", postId);
        parameters.put("createdAt", LocalDateUtil.strToLocalDateTime(null));
        parameters.put("updatedAt", LocalDateUtil.strToLocalDateTime(null));

        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
        comment.setId(key.longValue());
        return String.valueOf(comment.getId());
    }

    public String updateComment(Long id, Comment comment) {
        return null;
    }

    public int deleteComment(Long postId, long id) {
//        딜리트 쿼리문 작성, delete를 하기위해서는 sql delete문을 작성해야함
        String sql = "delete from comments where id=? and post_id = ?";

//        sql에 id랑 postid를 담아서 그것을 upadte로 실행시킨다
        return jdbcTemplate.update(sql, id, postId);
    }

}
