package com.ssafy.trip.host;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;

/**
 * 호스트 데이터 접근 객체 인터페이스
 */
@Mapper
public interface HostDao {
    /**
     * 새 호스트를 등록합니다.
     */
    int insert(Host host);

    /**
     * 호스트 ID로 호스트를 조회합니다.
     */
    Host getHostById(Long hostId);
    
    /**
     * 모든 호스트 목록을 조회합니다.
     */
    List<Host> getHosts() throws SQLException;
    
    /**
     * 호스트 정보를 업데이트합니다.
     */
    int updateHost(Host host);
    
    /**
     * 호스트를 삭제합니다.
     */
    int deleteHost(Long hostId);
    
    /**
     * 호스트 상태를 업데이트합니다.
     */
    int updateHostStatus(Long hostId, String hostStatus);
    
    /**
     * 사용자 ID로 호스트를 조회합니다.
     */
    Host getHostByUserId(Long userId);
}