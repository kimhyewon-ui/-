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

@Controller
public class BoardController {

    private final BoardService boardService;

    private final ObjectMapper objectMapper;

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
            @RequestParam(required = false, defaultValue = "") String searchParam,
            @RequestParam(defaultValue = "1") int page, Model model) {
        // KEY String : value : Object
        System.out.println(searchParam);
        page = page < 1 ? 1 : page;
        Pagination pagination = new Pagination(new ParamVo());
        pagination.setTotalCount(boardService.countAll(searchParam));
        model.addAttribute("pagination", pagination);

        model.addAttribute("count", boardService.countAll(searchParam));
        model.addAttribute("boards", boardService.getBoardList(sortItems, sortFlags, page, searchParam));
        model.addAttribute("sortFlag", sortFlags);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("page", page);
        return "list";
    }

    @GetMapping(value ="/form")
    public  String create(Model model) {
        model.addAttribute("board", new Board());
        return "form";
    }

    @PostMapping(value="/form") //생성
    public String save(Board board) {

        boardService.save(board);

        return "redirect:/";
    }

    @GetMapping(value = "view/{id}") //읽기
//    PathVariable : url에서 각 구분자에 들어오는 값을 처리해야 할 때, index/1 이런식
    public String findOne(@PathVariable Long id, Model model) {
        Map<String, Object> returnMap = new HashMap<>();

        Pagination pagination = new Pagination(new ParamVo());
        model.addAttribute("pagination", pagination);

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

        boardService.update(id, board);
        return "redirect:/view/" + id;
    }

    @PostMapping(value="/{id}/delete") //삭제
    //    PathVariable : url에서 각 구분자에 들어오는 값을 처리해야 할 때, index/1 이런식
    public Object delete(@PathVariable Long id) {
        boardService.deleteById(id);
        return "redirect:/";
    }
}
