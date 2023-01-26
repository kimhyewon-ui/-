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

    public int countAll() {
        return boardRepository.countAll();
    }

    public int countById(long id) {
        return boardRepository.countById(id);
    }

    public List<Board> getBoardListWithComments(String sortParams, String sortFlags, int page) {
        return boardRepository.findListWithComments(sortParams, sortFlags, page);
    }

    public Board findByIdWithComments(Long id) {
        return boardRepository.findByIdWithComments(id);
    }

    public Board save(Board board) {
        return boardRepository.save(board);
    }

    public int update(Long id, Board board) {
        return boardRepository.update(id, board);
    }

    public int deleteById(Long id) {
        return boardRepository.deleteById(id);
    }
}
