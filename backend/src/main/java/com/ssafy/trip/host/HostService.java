package com.ssafy.trip.host;

import java.sql.SQLException;
import java.util.List;

/**
 * 호스트 서비스 인터페이스
 */
public interface HostService {

    /**
     * 새 호스트를 등록합니다.
     */
    int registHost(Host host) throws SQLException;
    
    /**
     * 호스트 ID로 호스트를 조회합니다.
     */
    Host getHostById(Long hostId) throws SQLException;
    
    /**
     * 모든 호스트 목록을 조회합니다.
     */
    List<Host> getHostList() throws SQLException;
    
    /**
     * 호스트 정보를 업데이트합니다.
     */
    int updateHost(Host host) throws SQLException;
    
    /**
     * 호스트를 삭제합니다.
     */
    int deleteHost(Long hostId) throws SQLException;
    
    /**
     * 호스트 상태를 업데이트합니다.
     */
    int updateHostStatus(Long hostId, String hostStatus) throws SQLException;
    
    /**
     * 사용자 ID로 호스트를 조회합니다.
     */
    Host getHostByUserId(Long userId) throws SQLException;
    
}