package org.roomtraveler.roomtrip.repository.search;

import org.roomtraveler.roomtrip.domain.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardSearch {
    Page<Board> search1(Pageable pageable);

    //types는 여러 조건의 조합이 가능하도록
    Page<Board> searchAll(String[] types, String keyword, Pageable pageable);
}
