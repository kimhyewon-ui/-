package com.example.board.api.board.controller;

import com.example.board.api.board.entity.Board;
import com.example.board.api.board.service.BoardService;
import com.example.board.api.common.Vo.ParamVo;
import com.example.board.core.utils.Criteria;
import com.example.board.core.utils.Pagination;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    private final ObjectMapper objectMapper;

//    ObjectMapper: rest api로 개발한거라서 json형식으로 리턴해주려고 사용함
    public BoardController(BoardService boardService, ObjectMapper objectMapper) {
        this.boardService = boardService;
        this.objectMapper = objectMapper;

    }

    @GetMapping(value = "")
    public Object findAll(
            @RequestParam(required = false, defaultValue = "id") String sortItems,
            @RequestParam(required = false, defaultValue = "DESC") String sortFlags) {
        // KEY String : value : Object
        Map<String, Object> returnMap = new HashMap<>();
        log.debug("/ start");
        Pagination pagination = new Pagination(new ParamVo());
        pagination.setTotalCount(boardService.countAll());
        returnMap.put("pagination", pagination);
        returnMap.put("count", boardService.countAll());
        returnMap.put("boards", boardService.getBoardListWithComments(sortItems, sortFlags));
        return "board";

        /**
         * <th:block th:each="board: ${boards}">
         *     <div>board.title</div>
         * </th:block>
         */
    }

    @PostMapping(value="/new")
    public Object save(@RequestBody Board board) {
//        list, map등 자료구조, 담아주려고 사용했다
//        Map은 키와 벨류로 나눠서 사용할수가 있고 연관관계가 있음, list는 하나의 단어로만 가능하다(String)
        Map<String, Object> returnMap = new HashMap<>();
        log.debug("/new start");
        if (boardService.save(board) != null) {
            returnMap.put("success", 1);
        } else {
            returnMap.put("success", 0);
        }
        return "redirect/들어가야함";
    }

    @GetMapping(value = "/{id}")
    public Object findOne(@PathVariable Long id) {
        Map<String, Object> returnMap = new HashMap<>();
        log.debug("/{id} start");
        Pagination pagination = new Pagination(new ParamVo());
        pagination.setTotalCount(boardService.countById(id));
        returnMap.put("pagination", pagination);
        returnMap.put("count", boardService.countById(id));
        returnMap.put("return", boardService.findByIdWithComments(id));
        return "redirect/들어가야함";
    }


    @PostMapping(value="/{id}/update")
    public Object update(@PathVariable Long id, @RequestBody Board board) {
        Map<String, Object> returnMap = new HashMap<>();
        log.debug("/{id}/update start");
        returnMap.put("success", boardService.update(id, board));
        return "redirect/들어가야함";
    }

    @PostMapping(value="/{id}/delete")
    public Object delete(@PathVariable Long id) {
        Map<String, Object> returnMap = new HashMap<>();
        log.debug("/{id}/delete start");
        returnMap.put("success", boardService.deleteById(id));
        return "redirect/들어가야함";
    }
}
