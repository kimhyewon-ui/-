package com.example.board.api.board.repository;

import com.example.board.api.board.entity.Board;
import com.example.board.api.comment.entity.Comment;
import com.example.board.api.comment.repository.CommentRepository;
import com.example.board.core.utils.LocalDateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class  BoardRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRepository commentRepository;



    // 전체 갯수를 가져옴.
    public int countAll(String searchParam) {
        if (searchParam.equals(""))
            return jdbcTemplate.queryForObject("select count(*) from posts", Integer.class);
        else
            return jdbcTemplate.queryForObject("select count(*) from posts where (title LIKE '%" + searchParam +"%' OR content LIKE '%" + searchParam+ "%')", Integer.class, searchParam);
    }

//    리스트와 코멘트를 함께 정렬해준다
    public List<Board> findList(String sortItems, String sortFlags, int page, String searchParam) {
//        보드에 있는 정렬할 아이템이라는것을 posts를 붙여서 알려줌. 아니면 코멘트의 값과 혼동하기때문에
        String sortParam = sortItems;
        String sort = sortFlags;
//        StringBuilder: 변경 가능한 문자열

//        order_by는 정렬쿼리를 만들기 위한 변수를 선언한거
        StringBuilder order_by;
//        "order by"를 앞에 붙여서 보드에 있는 아이템들을 정렬하겠다
        order_by = new StringBuilder("order by " + sortParam + " " + sort);
        StringBuilder pagination;
        int offset = page <= 1 ? 0 : (page -1) * 10;
        pagination = new StringBuilder("limit " + 10 + " offset " + offset);
//        보드와 코멘트의 데이터를 매칭하여 보드의 id값과 코멘트의 아이디 값을 함께 정렬하는 쿼리문에 대한 결과를 객체로 리턴합니다.
        String sql = "";
        if (searchParam.equals("")) {
            sql = "select * from posts " + order_by + " " + pagination;
            System.out.println(sql);
            return jdbcTemplate.query(sql, new BoardRowMapper());
        } else {
            sql = "select * from posts where (title LIKE '%" + searchParam +"%' OR content LIKE '%" + searchParam+ "%') " + order_by + " " + pagination;
            System.out.println(sql);
            return jdbcTemplate.query(sql, new BoardRowMapper());
        }
//      쿼리문에 대한 결과를 객체로 리턴합니다

    }

    public Board findByIdWithComments(Long id) {
//        posts에서 comments의 데이터를 매칭하고, 매칭되는 posts.id값을 반환, 코멘트의 post_id....
//        게시판 리스트에서 댓글의 아이디와 게시글의 아이디를 조인해서 함께 가져온다
//        그 중 파라미터로 받은 ID 값과 동일한 id값을 가진 게시글 하나를 가져온다.
        String sql = "select * from posts LEFT OUTER JOIN comments c on posts.id = c.post_id where posts.id = ?";

//        위에서 선언한 sql쿼리문에 대한 결과를 객체로 리턴한다
        return jdbcTemplate.query(sql, new BoardRowMapper2(), id).get(0);
    }

    public void save(Board board) {
        String find_sql = "select * from posts order by id DESC";
        Board tempBoard = jdbcTemplate.query(find_sql, new BoardRowMapper()).get(0);

//        글을 작성하면 작성한 글의 타이틀과, 콘텐츠, 날짜를 가져와서 파라미터 변수에 넣어주고
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("id", tempBoard.getId()+1);
        parameters.put("title", board.getTitle());
        parameters.put("content", board.getContent());
        parameters.put("createdAt", LocalDateUtil.strToLocalDateTime(null));
        parameters.put("updatedAt", LocalDateUtil.strToLocalDateTime(null));

        String save_sql = "insert into posts (id, title, content, createdAt, updatedAt) values (?, ?, ?, ?, ?)";

        jdbcTemplate.update(save_sql,
                parameters.get("id"),
                parameters.get("title"),
                parameters.get("content"),
                parameters.get("createdAt"),
                parameters.get("updatedAt"));

    }

    public int update(Long id, Board board) {
//        보드를 업데이트 하면 아이디 값을 받아서 업데이트 된 제목, 내용을 정렬한다
        String sql = "update posts set title=?, content=?, updatedAt=? where id=?";

//      수정한 내용을 데이터 베이스에 저장한다
        return jdbcTemplate.update(sql,
                board.getTitle(),
                board.getContent(),

                LocalDateUtil.strToLocalDateTime(null), // -> 2022-12-20 20:19:00
                id);
    }

    public int deleteById(Long id) {
//       파라미터로 받은 id값과 동일한 아이디를 가진 게시글을 지운다
        String sql = "delete from posts where id=?";

//        INSERT, UPDATE, DELETE쿼리는 update()메서드를 사용.
        return jdbcTemplate.update(sql, id);
    }

    public class BoardRowMapper implements ResultSetExtractor<List<Board>> {

        @Override
        public List<Board> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Number, Board> items = new HashMap<>();
            List<Number> idList = new ArrayList<>();
            List<Board> returnList = new ArrayList<>();

            int cnt = 1;
            int previousBoardId = -1;
            // sql문의 결과를 rs로 받아오게 되는데 해당하는 내용에 맞게 객체를 생성하고, 객체에 맵핑을 해주어 반환.
            while (rs.next()) {

//                정렬을 해주기 위해서 값을 넣어준다
//                해당하는 리스트안에 값이 있는지 유무 확인
                if (!idList.contains(rs.getInt("id"))) {
//                    posts_id값이 없거나 중복이 아니면 넣어줘라
                    idList.add(rs.getInt("id"));
                }

//                   정의
                Board board = new Board();
                board.setId(rs.getInt("id"));
                board.setTitle(rs.getString("title"));
                board.setContent(rs.getString("content"));
                board.setCommentCnt(commentRepository.commentCntByPostId(rs.getInt("id")));
                board.setCreatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("createdAt")));
                board.setUpdatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("updatedAt")));

                items.put(rs.getInt("id"), board);
            }

//             정렬용 반복문
            for (Number i : idList) {
                Board board = items.get(i);
                returnList.add(board);
            }

            return returnList;
        }
    }

    public class BoardRowMapper2 implements ResultSetExtractor<List<Board>> {

        @Override
        public List<Board> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Number, Board> items = new HashMap<>();
            List<Number> idList = new ArrayList<>();
            List<Board> returnList = new ArrayList<>();

            int cnt = 1;
            int previousBoardId = -1;
            // sql문의 결과를 rs로 받아오게 되는데 해당하는 내용에 맞게 객체를 생성하고, 객체에 맵핑을 해주어 반환.
            while (rs.next()) {

//                정렬을 해주기 위해서 값을 넣어준다
//                해당하는 리스트안에 값이 있는지 유무 확인
                if (!idList.contains(rs.getInt("posts.id"))) {
//                    posts_id값이 없거나 중복이 아니면 넣어줘라
                    idList.add(rs.getInt("posts.id"));
                }

                Comment comment = new Comment();
//                이전board id값
                if (rs.getInt("posts.id") != previousBoardId) {
                    cnt = 1;
//                   정의
                    Board board = new Board();
                    board.setId(rs.getInt("posts.id"));
                    board.setTitle(rs.getString("title"));
                    board.setContent(rs.getString("content"));
                    board.setCommentCnt(commentRepository.commentCntByPostId(rs.getInt("posts.id")));
                    board.setCreatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("createdAt")));
                    board.setUpdatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("updatedAt")));

                    comment.setId(rs.getInt("c.id"));
                    comment.setName("익명" + cnt);
                    comment.setBody(rs.getString("c.body"));

//                    댓글이 있으면
                    if (rs.getInt("c.id") != 0) {
//                        singletonList는 하나의 객체만 포함하는 불변의 리스트(size가 1로 고정)
                        board.setComments(Collections.singletonList(comment));
                    }
//                    아이디값을 기준으로 맵에 넣어준다 -> 밑에 items에서 꺼내오려고 넣어준것
                    items.put(rs.getInt("posts.id"), board);
                    previousBoardId = (int) board.getId();
                } else {
//                    게시글에 이미 작성된 댓글이 있으면 기존 게시글에 댓글을 더해준다
                    cnt+=1;

                    Board board = items.get(previousBoardId);
                    comment.setId(rs.getInt("c.id"));
                    comment.setName("익명" + cnt);
                    comment.setBody(rs.getString("c.body"));

                    List<Comment> temp = new ArrayList<>(board.getComments());
                    temp.add(comment);
                    board.setComments(temp);
                    items.put(rs.getInt("posts.id"), board);
                }
            }



//             정렬용 반복문
            for (Number i : idList) {
                Board board = items.get(i);
                returnList.add(board);
            }

            return returnList;
        }
    }


}
