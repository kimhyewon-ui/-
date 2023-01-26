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
public class BoardController {

    private final BoardService boardService;

    private final ObjectMapper objectMapper;

//    ObjectMapper: rest api로 개발한거라서 json형식으로 리턴해주려고 사용함
    public BoardController(BoardService boardService, ObjectMapper objectMapper) {
        this.boardService = boardService;
        this.objectMapper = objectMapper;

    }

    @GetMapping(value = "")
    public String findAll(
//            키값이 존재하지 않으면 에러가 발생하게 되는데, 이를 방지하고자 defaultValue 값을 설정해줌
//            required = false : 키값이 존재하지 않다고 해서 에러가 발생하지 않게, 만약 키값이 존재하지 않다면 String sortItems에
//            defaultValue값으로 id값이 들어가게 된다
            @RequestParam(required = false, defaultValue = "id") String sortItems,
            @RequestParam(required = false, defaultValue = "DESC") String sortFlags,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        // KEY String : value : Object
        log.debug("/ start");
        Pagination pagination = new Pagination(new ParamVo());
        pagination.setTotalCount(boardService.countAll());
        model.addAttribute("pagination", pagination);
        model.addAttribute("count", boardService.countAll());
        model.addAttribute("boards", boardService.getBoardListWithComments(sortItems, sortFlags, page));
        model.addAttribute("sortFlag", sortFlags);
        return "list";
    }

    @GetMapping(value ="/form")
    public  String create(Model model) {
        model.addAttribute("board", new Board());
        return "form";
    }

    @PostMapping(value="/form") //생성
//    @RequestBody : 서버에 요청을 보낼 때 필요한 데이터를 담아서 보내는 공간이 Body이다.
    public String save(Board board) {
//        list, map등 자료구조, 담아주려고 사용했다
//        Map은 키와 벨류로 나눠서 사용할수가 있고 연관관계가 있음, list는 하나의 단어로만 가능하다(String)
        log.debug("/new start");
        boardService.save(board);

        return "redirect:/";
    }

    @GetMapping(value = "view/{id}") //읽기
//    PathVariable : url에서 각 구분자에 들어오는 값을 처리해야 할 때, index/1 이런식
    public String findOne(@PathVariable Long id, Model model) {
        Map<String, Object> returnMap = new HashMap<>();
        log.debug("/{id} start");
        Pagination pagination = new Pagination(new ParamVo());
        pagination.setTotalCount(boardService.countById(id));
        model.addAttribute("pagination", pagination);
        model.addAttribute("count", boardService.countById(id));
        model.addAttribute("board", boardService.findByIdWithComments(id));
        return "view";
    }

    @GetMapping(value = "/{id}/update")
    public Object update(@PathVariable Long id, Model model) {
        model.addAttribute("board", boardService.findByIdWithComments(id));
        return "reform";
    }

    @PostMapping(value="/{id}/update") //업데이트
    //    PathVariable : url에서 각 구분자에 들어오는 값을 처리해야 할 때, index/1 이런식
    public Object update(@PathVariable Long id, Board board) {
        Map<String, Object> returnMap = new HashMap<>();
        log.debug("/{id}/update start");
        returnMap.put("success", boardService.update(id, board));
        return "redirect:/view/" + id;
    }

    @PostMapping(value="/{id}/delete") //삭제
    //    PathVariable : url에서 각 구분자에 들어오는 값을 처리해야 할 때, index/1 이런식
    public Object delete(@PathVariable Long id) {
        Map<String, Object> returnMap = new HashMap<>();
        log.debug("/{id}/delete start");
        returnMap.put("success", boardService.deleteById(id));
        return "redirect:/";
    }
}
