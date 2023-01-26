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

@Slf4j
@Repository
public class  BoardRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRepository commentRepository;


//    쿼리를 이용해서 보드에 카운트를 다 선택, class가 integer인것만
    // 전체 갯수를 가져옴.
    public int countAll() {
        return jdbcTemplate.queryForObject("select count(*) from posts", Integer.class);
    }


//    쿼리문을 날려서 integer인 id값을 가져온다
    // 한개 갯수를 가져옴.
    public int countById(long id) {
        return jdbcTemplate.queryForObject("select count(*) from posts where posts.id = ?", Integer.class, id);
    }

//    리스트랑 코멘트를 같이 찾음.
    public List<Board> findListWithComments(String sortItems, String sortFlags, int page) {
//        보드에 있는 정렬할 아이템이라는것을 posts를 붙여서 알려줌. 아니면 코멘트의 값과 혼동하기때문에
        String sortParam = "posts." + sortItems;
        String sort = sortFlags;
//        StringBuilder: 변경 가능한 문자열
        StringBuilder order_by;
//        "order by"를 앞에 붙여서 보드에 있는 아이템들을 정렬하겠다
        order_by = new StringBuilder("order by " + sortParam + " " + sort);
        // "order by posts.id DESC"
//        sortItems에 ,가 있으면
        if (sortItems.contains(",")) {
//           split을 이용해서 ,를 버림
            List<String> sortFlagList = Arrays.asList(sortFlags.split(","));
            order_by = new StringBuilder().append("order by ");
            int cnt = 0;
//            ,를 뺀 아이템 값들을 변수에 넣어준다
            for (String sortItem : sortItems.split(",")) {
//                정렬할 아이템에 posts 문자열이 있으면
                if (sortItem.contains("posts."))
//                 posts 문자열을 공란으로 바꾼다
                    sortItem = sortItem.replace("posts.", "");

                //                cnt를 오름차순으로 정렬한것을 objects와 비교하여
                if (Objects.equals(sortFlagList.get(cnt), "DESC")) {
                    sort = "DESC";
                } else {
                    sort = "ASC";
                }
//                게시글이라는걸 명확하게 표시하기 위해서 posts를 붙여줬다.
//                번호를 1씩 증가하면서.. 보드에 있는 제목, 날짜, 번호들을 오름차순으로 정렬한다
                order_by.append("posts.").append(sortItem).append(" ").append(sort);
                order_by.append(", ");
                cnt += 1;
            }
            // "order by posts.id DESC, posts.title DESC, "
            order_by.delete(order_by.length() - 2, order_by.length() - 1);
            // -> "order by posts.id DESC, posts.title DESC 콤마랑 공백 없애주려고"
        }
//        order_by 정렬해줄 때 쓰는 쿼리문, 보드를 정렬
        String sql = "select * from posts LEFT OUTER JOIN comments c on posts.id = c.post_id " + order_by;
        log.debug("findList query = {}", sql);

        return jdbcTemplate.query(sql, new BoardRowMapper2());
    }

    public Board findByIdWithComments(Long id) {
//        posts에서 comments의 데이터를 매칭하고, 매칭되는 posts.id값을 반환, 코멘트의 post_id....
        String sql = "select * from posts LEFT OUTER JOIN comments c on posts.id = c.post_id where posts.id = ?";
        log.debug("findById query = {}", sql);

        return jdbcTemplate.query(sql, new BoardRowMapper2(), id).get(0);
    }

    public Board save(Board board) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
//        jdbc템플릿에 접근해서 posts에 테이블에 삽입해라, id칼럼의 값을 key로 반환해라
        jdbcInsert.withTableName("posts").usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", board.getTitle());
        parameters.put("content", board.getContent());
        parameters.put("createdAt", LocalDateUtil.strToLocalDateTime(null));
        parameters.put("updatedAt", LocalDateUtil.strToLocalDateTime(null));

        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters)); // 저장
        board.setId(key.longValue()); // 저장된 게시글의 key = id 값을 받아 BOARD 객체에 세팅
        return board; // 객체리턴
    }

    public int update(Long id, Board board) {
//        보드를 업데이트 하면 아이디행 값을 받아서 업데이트 된 제목, 내용을 정렬한다
        String sql = "update posts set title=?, content=?, updatedAt=? where id=?";
        log.debug("update query = {}", sql);

        return jdbcTemplate.update(sql,
                board.getTitle(),
                board.getContent(),
//                null로 초기화?
                LocalDateUtil.strToLocalDateTime(null), // -> 2022-12-20 20:19:00
                id);
    }

    public int deleteById(Long id) {
//        보드에서 게시물을 지우면 그 아이디값을 받아와서 업데이트한다
        String sql = "delete from posts where id=?";

//        INSERT, UPDATE, DELETE쿼리는 update()메서드를 사용.
        return jdbcTemplate.update(sql, id);
    }

    public class BoardRowMapper2 implements ResultSetExtractor<List<Board>> {

        @Override
        public List<Board> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Number, Board> items = new HashMap<>();
            List<Number> idList = new ArrayList<>();
            List<Board> returnList = new ArrayList<>();
            int cnt = 1;
            int previousBoardId = -1;
            while (rs.next()) {

                if (!idList.contains(rs.getInt("posts.id"))) {
                    idList.add(rs.getInt("posts.id"));
                }

                Comment comment = new Comment();
                if (rs.getInt("posts.id") != previousBoardId) {
                    cnt = 1;
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
                    comment.setCreatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("c.createdAt")));
                    comment.setUpdatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("c.updatedAt")));
                    if (rs.getInt("c.id") != 0) {
//                        singletonList는 하나의 객체만 포함하는 불변의 리스트(size가 1로 고정)
                        board.setComments(Collections.singletonList(comment));
                    }
                    items.put(rs.getInt("posts.id"), board);
                    previousBoardId = (int) board.getId();
                } else {
                    cnt+=1;
                    Board board = items.get(previousBoardId);
                    comment.setId(rs.getInt("c.id"));
                    comment.setName("익명" + cnt);
                    comment.setBody(rs.getString("c.body"));
                    comment.setCreatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("c.createdAt")));
                    comment.setUpdatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("c.updatedAt")));

                    List<Comment> temp = new ArrayList<>(board.getComments());
                    temp.add(comment);
                    board.setComments(temp);
                }
            }


            // 정렬용 반복문
            for (Number i : idList) {
                Board board = items.get(i);
                returnList.add(board);
            }

            return returnList;
        }
    }


}
