package org.roomtraveler.roomtrip.repository.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.roomtraveler.roomtrip.domain.Board;
import org.roomtraveler.roomtrip.domain.QBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class BoardSearchImpl extends QuerydslRepositorySupport implements BoardSearch {

    public BoardSearchImpl(){
        super(Board.class);
    }

    @Override
    public Page<Board> search1(Pageable pageable) {

        QBoard board = QBoard.board;

        JPQLQuery<Board> query = from(board);

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.or(board.title.contains("11")); // 제목 like

        booleanBuilder.or(board.content.contains("11")); // 내용 like

        query.where(booleanBuilder);
        query.where(board.bno.gt(0L));

        query.where(board.title.contains("1"));

        //페이징
        this.getQuerydsl().applyPagination(pageable, query);

        List<Board> list = query.fetch();

        long count = query.fetchCount();

        return null;

    }

    @Override
    public Page<Board> searchAll(String[] types, String keyword, Pageable pageable) {
        QBoard board = QBoard.board;
        JPQLQuery<Board> query = from(board);

        //검색조건과 키워드가 있다면
        //types는 제목(t) 내용(c) 작성자(w)
        if( (types != null && types.length > 0) && keyword != null){

            BooleanBuilder booleanBuilder = new BooleanBuilder(); //(

            for(String type : types){

                switch(type){
                    case "t" -> booleanBuilder.or(board.title.contains(keyword));
                    case "c" -> booleanBuilder.or(board.content.contains(keyword));
                    case "w" -> booleanBuilder.or(board.writer.contains(keyword));
                }
            } // end for
            query.where(booleanBuilder);
        } // end if

        //bno > 0
        query.where(board.bno.gt(0L));

        //paging
        this.getQuerydsl().applyPagination(pageable, query);

        List<Board> list = query.fetch();

        long count = query.fetchCount();


        //List<T> 실제 목록 데이터
        //Pageable : 페이지 관련 정보를 가진 객체
        //long 전체 개수
        return new PageImpl<>(list, pageable, count);
    }
}
