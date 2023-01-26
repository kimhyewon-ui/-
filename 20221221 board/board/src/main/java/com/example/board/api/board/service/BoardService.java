package com.example.board.api.board.service;

import com.example.board.api.board.entity.Board;
import com.example.board.api.board.repository.BoardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    public int countAll(String searchParam) {
        return boardRepository.countAll(searchParam);
    }


    public List<Board> getBoardList(String sortParams, String sortFlags, int page, String searchParam) {
        return boardRepository.findList(sortParams, sortFlags, page, searchParam);
    }

    public Board findByIdWithComments(Long id) {
        return boardRepository.findByIdWithComments(id);
    }

    public void save(Board board) {boardRepository.save(board);
    }

    public int update(Long id, Board board) {
        return boardRepository.update(id, board);
    }

    public int deleteById(Long id) {
        return boardRepository.deleteById(id);
    }
}
