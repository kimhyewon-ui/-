package com.example.board.core.utils;

import com.example.board.api.common.Vo.ParamVo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Data
@RequiredArgsConstructor
public class Pagination {

    private int displayPageCnt = 10;
    private int totalDataCount;
    private int startPage;
    private int endPage;
    private int firstPage;
    private int lastPage;
    private boolean prev;
    private boolean next;
    private int totalPage;

    private ParamVo paramVo;

    public Pagination(ParamVo paramVo) {
        this.paramVo = paramVo;
    }

    // 전체 게시물의 수를 입력 받음
    public void setTotalCount(int totalDataCount) {
        this.totalDataCount = totalDataCount;
        calcData();
    }

    // startPage, endPage, prev, next를 계산
    public void calcData() {
        int page = this.paramVo.getPage();
        int perPageNum = this.paramVo.getListSize();

        /*
         * 예: 현재 페이지가  13이면 13/10 = 1.3 -> 2, 끝페이지는 2 * 10 = 20
         * ceil - 반올림
         */
        this.endPage = (int) (Math.ceil(page / (double) displayPageCnt) * displayPageCnt);

        /*
         * 예: 현재 페이지가 13이면 20 - 10 + 1 = 11
         */
        this.startPage = (this.endPage - displayPageCnt) + 1;

        // 실제로 사용되는 페이지의 수
        // 예: 전체 게시물의 수가 88개이면 88/10 = 8.8 올림 -> 9
        int tempEndpage = (int) (Math.ceil(totalDataCount / (double) perPageNum));
        totalPage = tempEndpage;

        if (totalPage > 1000) {
            totalPage = 1000;
        }

        if (this.endPage > tempEndpage) {
            this.endPage = tempEndpage;
        }

        this.firstPage = 10 * (page - 1) + 1;

        if (page != this.totalPage)
            this.lastPage = 10 * (page);
        else
            this.lastPage = totalDataCount;

        this.prev = (startPage != 1);    // startPage가 1이 아니면 False
        this.next = (endPage * perPageNum < totalDataCount);    // 아직 더 보여줄 페이지가 있으니 true
    }

}
