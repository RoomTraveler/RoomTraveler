package com.ssafy.trip.host.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ssafy.trip.host.model.Host;
import org.apache.ibatis.annotations.Mapper;

/**
 * 호스트 데이터 접근 객체 인터페이스
 */
@Mapper
public interface HostDao {
    /**
     * 새 호스트를 등록합니다.
     */
    int insert(Host host) throws SQLException;

    /**
     * 호스트 ID로 호스트를 조회합니다.
     */
    Host getHostById(Long hostId) throws SQLException;

    /**
     * 모든 호스트 목록을 조회합니다.
     */
    List<Host> getHosts() throws SQLException;

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
    int updateHostStatus(Long hostId, String status) throws SQLException;

    /**
     * 특정 상태의 호스트 목록을 조회합니다.
     */
    List<Host> getHostsByStatus(String status) throws SQLException;

    /**
     * 호스트 상태와 사유를 업데이트합니다.
     */
    int updateHostStatusAndReason(Long hostId, String status, String reason) throws SQLException;

    /**
     * 사용자 ID로 호스트를 조회합니다.
     */
    Host getHostByUserId(Long userId) throws SQLException;

    /**
     * 사업자 번호로 호스트를 조회합니다.
     */
    Host getHostByBusinessNumber(String businessNumber) throws SQLException;

    /**
     * 상태 및 페이징 조건에 따라 호스트 목록을 조회합니다.
     */
    List<Host> getHostsWithPaginationAndStatus(Map<String, Object> params) throws SQLException;

    /**
     * 상태 조건에 맞는 호스트의 총 수를 조회합니다.
     */
    int countHostsWithStatus(Map<String, Object> params) throws SQLException;
}