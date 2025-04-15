package org.roomtraveler.roomtrip.service;

import org.roomtraveler.roomtrip.dto.BoardDTO;
import org.roomtraveler.roomtrip.dto.PageRequestDTO;
import org.roomtraveler.roomtrip.dto.PageResponseDTO;

public interface BoardService {

    Long register(BoardDTO boardDTO);

    BoardDTO readOne(Long bno);

    void modify(BoardDTO boardDTO);

    void delete(Long bno);

    PageResponseDTO<BoardDTO> list(PageRequestDTO pageRequestDTO);
}
