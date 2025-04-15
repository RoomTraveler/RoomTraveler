package org.roomtraveler.roomtrip.repository;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.roomtraveler.roomtrip.domain.Board;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


@SpringBootTest
@Log4j2
public class BoardRepositoryTests {

    @Autowired
    private BoardRepository boardRepository;


    @Test
    public void testInsert(){
        IntStream.rangeClosed(1,100).forEach(i -> {
            Board board = Board.builder()
                    .title("title..." +i)
                    .content("content..." +i)
                    .writer("user"+ (i % 10))
                    .build();

            Board result = boardRepository.save(board); //sav()를 통해서 insert 실행
            //save()는 현재의 영속 컨텍스트 내에 데이터가 존재하는지 찾아보고 해당 엔티티 객체가 없을 때는 insert를
            //존재할 때는 update를 자동으로 실행

            //Save()의 결과는 db에 저장된 데이터와 동기화된 Board 객체가 반환. 최종적으로 100개 데이터 생성
            //테스트코드는 @id값이 null이니 insert만 ㄴ실행.
            log.info("BNO: " + result.getBno());
        });
    }

    @Test
    public void testSelect(){
        Long bno = 100L;

        //게시물 조회할 때는 findById()를 이용해서 처리
        //findById()의 리턴 타입은 Optional<T>
        Optional<Board> result = boardRepository.findById(bno);

        Board board = result.orElseThrow();

        log.info(board);
    }

    //Update는 등록 시간이 필요하므로 가능하면 findById로 가져온 객체를 이용해서 약간의 수정을 통해 처리하도록 하자
    //일반적으로 엔티티 객체는 가능하면 최소한의 변경이나 변경이 없는 불변(immutable)하게 설계하는게 좋지만 반드시는 아님
    //Board 클래스에 수정이 가능한 부분을 미리 메소드로 설계. 제목./내용은 수정이 가능하니 change 메소드 추가
    @Test
    public void testUpdate(){
        Long bno = 100L;

        Optional<Board> result = boardRepository.findById(bno);

        Board board = result.orElseThrow();

        board.change("update...title 100","update content 100");

        boardRepository.save(board);
    }

    @Test
    public void testDelete(){
        Long bno = 1L;

        boardRepository.deleteById(bno);
    }

    @Test
    public void testPaging(){

        //1페이지부터 order by bno desc
        Pageable pageable = PageRequest.of(0,10, Sort.by("bno").descending());

        //리턴타입은 Page<T>
        Page<Board> result = boardRepository.findAll(pageable);

        log.info("Total count: "+ result.getTotalElements());
        log.info("Total Pages: "+ result.getTotalPages());
        log.info("page number: "+ result.getNumber());
        log.info("page size : "+ result.getSize());

        List<Board> todoList = result.getContent();

        todoList.forEach(board -> log.info(board));
    }


    @Test
    public void testSearch1() {

        //2 page order by bno desc
        Pageable pageable = PageRequest.of(1,10, Sort.by("bno").descending());

        boardRepository.search1(pageable);

    }

    @Test
    public void testSearchAll(){

        String[] types = {"t","c","w"};

        String keyword = "1";

        Pageable pageable = PageRequest.of(0,10,Sort.by("bno").descending());

        Page<Board> result = boardRepository.searchAll(types,keyword,pageable);

        //total pages
        log.info(result.getTotalPages());

        //page size
        log.info(result.getSize());

        //Page Number
        log.info(result.getNumber());

        //prev next
        log.info(result.hasPrevious() +" : " + result.hasNext());

        result.getContent().forEach(board -> log.info(board));
    }
}

