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
//    post_id 값을 받아서 코멘트가 몇개인지 보여준다?
    public int commentCntByPostId(long post_id) {
        return jdbcTemplate.queryForObject("select count(*) from comments where post_id = ?", Integer.class, post_id);
    }

    public String saveComment(Long postId, Comment comment) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
//        usingGeneratedKeyColumns: auto_increment를 통해 생성된 id값이 자동으로 comments 테이블에 입력됨
        jdbcInsert.withTableName("comments").usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "name");
        parameters.put("body", comment.getBody());
//        어떤 게시글에 코멘트를 달았는지 확인해야 하니까 ID값을 넣어준다
        parameters.put("post_id", postId);

//        별도의 쿼리문 없이 Map에 담아 넣어주면 key값에 해당하는 칼럼명에 value갑을 삽입한다.
//        executeAndReturnKey는 작업 수행과 동시에 자동 생성된 PK를 반환한다.
        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
//        longValue() : long 타입으로 받은 Value값
        comment.setId(key.longValue());
        return String.valueOf(comment.getId());
    }

    public int deleteComment(Long postId, long id) {
        String sql = "delete from comments where id=? and post_id = ?";

        return jdbcTemplate.update(sql, id, postId);
    }
}
