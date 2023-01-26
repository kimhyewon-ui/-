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
public class BoardRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRepository commentRepository;


    // 전체 갯수를 가져옴.
    public int countAll()

    {
        return jdbcTemplate.queryForObject("select count(*) from posts", Integer.class);
    }

//    jdbc템플릿에 있는 쿼리를 사용해서 보드 id값을 가져오는 명령문을 날려주는데 그걸 class가 integer인것으로 다 가져와서 countById 클라스에 담음
    // 한개 갯수를 가져옴.
    public int countById(long id) {
        return jdbcTemplate.queryForObject("select count(*) from posts where posts.id = ?", Integer.class, id);
    }


// board에 list와 comments를 함께 찾아서
    public List<Board> findListWithComments(String sortItems, String sortFlags) {
//        게시글의 아이디값이라는걸 posts를 붙여서 알려줌. 아니면 코멘트의 id값과 혼동하기때문에, 정렬해줄 Items.
        String sortParam = "posts." + sortItems;
//        sort는 정렬하는 방법
        String sort = sortFlags;
//        StringBuiler는 변경 가능한 문자열을 추가하는거
        StringBuilder order_by;
//        앞에 "order by"를 붙여서 posts.id를 DESC로 정렬해준다,
        order_by = new StringBuilder("order by " + sortParam + " " + sort);
        // "order by posts.id DESC"
//        sortItems에 "," 문자열이 포함되어 있는지 확인하는 함수
        if (sortItems.contains(",")) {
//            String을 담을 수 있는 list를 sortFlagList라는 변수에 담음, 거기에
//            배열에 asList라는 배열을 리스트로 만들어주는 메소드를 이용, 정렬을 하는데 콤마를 뺀다
            List<String> sortFlagList = Arrays.asList(sortFlags.split(","));
//            변경가능한 새로운 문자열객체를 만들어서 order by를 추가
            order_by = new StringBuilder().append("order by ");
            int cnt = 0;
//            반복문인데.. : 는 비교인가?
            for (String sortItem : sortItems.split(",")) {
//                정렬하는 아이템에 posts.이 있는지 확인하고 참이면
                if (sortItem.contains("posts."))
//                    sortItem의 posts 문자열을 공란으로 바꿔라?
                    sortItem = sortItem.replace("posts.", "");
//                sortFlagList에 있는 cnt를 가져와서 오브젝트에 있는 내용과 비교하여 역순으로 되어 있으면 정렬을 역순으로, 아니면 반대로
                if (Objects.equals(sortFlagList.get(cnt), "DESC")) {
                    sort = "DESC";
                } else {
                    sort = "ASC";
                }
//                게시글이라는걸 명확하게 표시하기 위해서 posts를 붙여줬다.
                order_by.append("posts.").append(sortItem).append(" ").append(sort);
                order_by.append(", ");
                cnt += 1;
            }
            // "order by posts.id DESC, posts.title DESC, "
            order_by.delete(order_by.length() - 2, order_by.length() - 1);
            // -> "order by posts.id DESC, posts.title DESC 콤마랑 공백 없애주려고"
        }
//        order_by 정렬해줄 때 쓰는 쿼리문
        String sql = "select * from posts " + order_by;
        log.debug("findList query = {}", sql);

        return jdbcTemplate.query(sql, new BoardRowMapper2());
    }


    public Board findByIdWithComments(Long id) {
//        보드의 전체값을 가져와서 코멘트의 아이디와 보드의 아이디값을 가져와서 조인??
        String sql = "select * from posts LEFT OUTER JOIN comments c on posts.id = c.post_id where posts.id = ?";
        log.debug("findById query = {}", sql);

//        stream()은 요소가 없을 때 null 대신 사용가능
        return jdbcTemplate.query(sql, new BoardRowMapper2(), id).stream().findFirst().get();
    }

    public Board save(Board board) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
//        withTableName은 어떤 테이블에 데이터를 삽입할지를 정하는것, usingGeneratedKeyColumns 어떤 칼럼을 자동으로 값을 받아올지 설정한다.
        jdbcInsert.withTableName("posts").usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", board.getTitle());
        parameters.put("content", board.getContent());
        parameters.put("description", board.getDescription());
        parameters.put("createdAt", LocalDateUtil.strToLocalDateTime(null));
        parameters.put("updatedAt", LocalDateUtil.strToLocalDateTime(null));

        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));
        board.setId(key.longValue());
        return board;
    }

    public int update(Long id, Board board) {
        String sql = "update posts set title=?, content=?, description=?, updatedAt=? where id=?";
        log.debug("update query = {}", sql);

        return jdbcTemplate.update(sql,
                board.getTitle(),
                board.getContent(),
                board.getDescription(),
                LocalDateUtil.strToLocalDateTime(null),
                id);
    }

    public int deleteById(Long id) {
        String sql = "delete from posts where id=?";

        return jdbcTemplate.update(sql, id);
    }

    public class BoardRowMapper2 implements ResultSetExtractor<List<Board>> {

        @Override
        public List<Board> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Number, Board> items = new HashMap<>();
            List<Number> idList = new ArrayList<>();
            List<Board> returnList = new ArrayList<>();

            int previousBoardId = -1;
            while (rs.next()) {
                if (!idList.contains(rs.getInt("posts.id"))) {
                    idList.add(rs.getInt("posts.id"));
                }
                Comment comment = new Comment();
                if (rs.getInt("posts.id") != previousBoardId) {

                    Board board = new Board();
                    board.setId(rs.getInt("posts.id"));
                    board.setTitle(rs.getString("title"));
                    board.setDescription(rs.getString("description"));
                    board.setContent(rs.getString("content"));
                    board.setCommentCnt(commentRepository.commentCntByPostId(rs.getInt("posts.id")));
                    board.setCreatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("createdAt")));
                    board.setUpdatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("updatedAt")));

                    returnList.add(board);
                    comment.setId(rs.getInt("c.id"));
                    comment.setName("익명" + comment.getId());
                    comment.setBody(rs.getString("c.body"));
                    comment.setEmail(rs.getString("c.email"));
                    comment.setCreatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("c.createdAt")));
                    comment.setUpdatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("c.updatedAt")));
                    if (rs.getInt("c.id") != 0) {
                        board.setComments(Collections.singletonList(comment));
                    }
                    items.put(rs.getInt("posts.id"), board);
                    previousBoardId = (int) board.getId();
                } else {
                    Board board = items.get(previousBoardId);
                    comment.setId(rs.getInt("c.id"));
                    comment.setName("익명" + comment.getId());
                    comment.setBody(rs.getString("c.body"));
                    comment.setEmail(rs.getString("c.email"));
                    comment.setCreatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("c.createdAt")));
                    comment.setUpdatedAt(LocalDateUtil.strToLocalDateTime(rs.getString("c.updatedAt")));

                    List<Comment> temp = new ArrayList<>(board.getComments());
                    temp.add(comment);
                    board.setComments(temp);
                }
            }
            for (Number i : idList) {
                Board board = items.get(i);
                returnList.add(board);
            }
            return returnList;
        }
    }


}
