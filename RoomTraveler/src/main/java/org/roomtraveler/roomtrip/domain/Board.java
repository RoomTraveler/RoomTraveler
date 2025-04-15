package org.roomtraveler.roomtrip.domain;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Board extends BaseEntity{

    /*
    IDENTItY : 데이터베이스에 위임(MYSQL/MariaDB)-auto_increment
    SQEUENCE:데이터베이스 시퀀스 오브젝트사용(ORACLE) - @SequenceGenerator 필요
    Table : 키 생성용 테이블 사용,모든 DB 사용 - @TableGenerator 필요
    AUTO:방언에 따라 자동 지정,기본 값
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;

    @Column(length = 500, nullable = false) //컬럼의 길이와 null허용여부
    private String title;


    @Column(length = 2000, nullable = false)
    private String content;

    @Column(length = 50, nullable = false)
    private String writer;

    //Update는 등록 시간이 필요하므로 가능하면 findById로 가져온 객체를 이용해서 약간의 수정을 통해 처리하도록 하자
    //일반적으로 엔티티 객체는 가능하면 최소한의 변경이나 변경이 없는 불변(immutable)하게 설계하는게 좋지만 반드시는 아님
    //Board 클래스에 수정이 가능한 부분을 미리 메소드로 설계. 제목./내용은 수정이 가능하니 change 메소드 추가
    public void change(String title, String content){
        this.title = title;
        this.content = content;
    }
}
