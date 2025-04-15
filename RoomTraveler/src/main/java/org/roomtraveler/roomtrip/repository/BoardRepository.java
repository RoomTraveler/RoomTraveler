package org.roomtraveler.roomtrip.repository;


import org.roomtraveler.roomtrip.domain.Board;
import org.roomtraveler.roomtrip.repository.search.BoardSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

//쿼리 메소드나 Querydsl을 이용
public interface BoardRepository extends JpaRepository<Board, Long>, BoardSearch { //엔티티 타입과 @Id 타입

    @Query(value = "select now()", nativeQuery = true)
    String getTime();
}
