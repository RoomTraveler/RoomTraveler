package com.ssafy.trip.host.service;

import com.ssafy.trip.host.model.Host;
import com.ssafy.trip.host.model.HostRegistrationRequestDto;
import com.ssafy.trip.host.model.HostUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 호스트 서비스 인터페이스
 */
public interface HostService {

    /**
     * 새 호스트를 등록합니다.
     */
    Host registerHost(Long userId, HostRegistrationRequestDto registrationDto, MultipartFile businessLicenseFile) throws SQLException, IOException;

    /**
     * 호스트 ID로 호스트를 조회합니다.
     */
    Host getHostById(Long hostId) throws SQLException;

    /**
     * 모든 호스트 목록을 조회합니다.
     */
    List<Host> getAllHosts() throws SQLException;

    /**
     * 호스트 정보를 업데이트합니다.
     */
    Host updateHostInfo(Long hostId, HostUpdateRequestDto updateDto) throws SQLException;

    /**
     * 호스트를 삭제합니다.
     */
    boolean deleteHost(Long hostId) throws SQLException;

    /**
     * 호스트 상태를 업데이트합니다.
     */
    void updateHostStatus(Long hostId, String status, String adminComment) throws SQLException;

    /**
     * 호스트 상태를 업데이트합니다. (관리자 코멘트 없음)
     */
    boolean updateHostStatus(Long hostId, String status) throws SQLException;

    /**
     * 특정 상태의 호스트 목록을 조회합니다.
     */
    List<Host> getHostsByStatus(String status) throws SQLException;

    /**
     * 호스트 상태와 거절 사유를 업데이트합니다.
     */
    boolean updateHostStatusAndReason(Long hostId, String status, String reason) throws SQLException;

    /**
     * 사용자 ID로 호스트를 조회합니다.
     */
    Host getHostByUserId(Long userId) throws SQLException;

    Host resubmitLicense(Long hostId, MultipartFile licenseResubmitFile) throws SQLException, IOException;

    void updateHost(Host host) throws SQLException;

    Page<Host> getHosts(Pageable pageable, String status) throws SQLException;

    boolean checkBusinessNumberExists(String businessNumber) throws SQLException;
}