package org.roomtraveler.roomtrip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass //공통되는 칼럼들 지정
@EntityListeners(value = { AuditingEntityListener.class }) //엔티티가 데이터베이스에 추가되거나 변경될 때 자동으로 시간 값 지정
@Getter
abstract public class BaseEntity {
//데이터가 추가된 시간이나 수정된 시간 등을 칼럼으로 작성
    @CreatedDate
    @Column(name = "regdate" , updatable = false)
    private LocalDateTime regDate;

    @LastModifiedDate
    @Column(name ="moddate")
    private LocalDateTime modDate;
}
