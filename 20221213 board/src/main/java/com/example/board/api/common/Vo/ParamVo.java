package com.example.board.api.common.Vo;

import lombok.Data;

@Data
public class ParamVo {

    // 페이지네이션
    private int page = 1;
    private int listSize =10;
}
