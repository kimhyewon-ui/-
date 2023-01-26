package com.example.board.core.utils;



import com.example.board.api.common.Vo.ParamVo;
import lombok.Data;
import org.springframework.web.util.UriComponentsBuilder;

@Data
public class Criteria {

    private int page;
    private int perPageNum;

    public Criteria(ParamVo paramVo) {
        this.page = paramVo.getPage();
        this.perPageNum = paramVo.getListSize();
    }
}
