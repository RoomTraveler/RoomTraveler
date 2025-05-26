package com.ssafy.trip.host.service;

import com.ssafy.trip.host.dao.HostDao;
import com.ssafy.trip.host.model.Host;
import com.ssafy.trip.host.model.HostRegistrationRequestDto;
import com.ssafy.trip.host.model.HostUpdateRequestDto;
import com.ssafy.trip.s3.AWSS3Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 호스트 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HostServiceImpl implements HostService {

    private final HostDao hostDao;
    private final AWSS3Service awsS3Service;
    // private final UserDao userDao; // User 테이블 스키마 및 역할 관리 정책에 따라 필요성 재검토

    /**
     * 새 호스트를 등록합니다.
     */
    @Override
    @Transactional
    public Host registerHost(Long userId, HostRegistrationRequestDto registrationDto, MultipartFile businessLicenseFile) throws SQLException, IOException {
        // HostRegistrationRequestDto에서 Host 엔티티로 변환
        Host host = new Host();
        host.setUserId(userId);
        host.setBusinessNumber(registrationDto.getBusinessNumber());
        host.setBusinessName(registrationDto.getBusinessName());
        host.setCeoName(registrationDto.getCeoName());
        host.setBusinessAddress(registrationDto.getBusinessAddress());
        host.setBusinessPhone(registrationDto.getBusinessPhone());
        host.setBusinessLicenseExpire(registrationDto.getBusinessLicenseExpire());
        host.setBankName(registrationDto.getBankName());
        host.setBankAccount(registrationDto.getBankAccount());
        host.setBankOwner(registrationDto.getBankOwner());
        host.setDescription(registrationDto.getDescription());
        host.setLatitude(registrationDto.getLatitude());
        host.setLongitude(registrationDto.getLongitude());
        host.setBusinessType(registrationDto.getBusinessType());

        host.setStatus("WAIT"); // 초기 상태는 WAIT (심사중)
        host.setCreatedAt(LocalDateTime.now());
        host.setUpdatedAt(LocalDateTime.now());

        if (businessLicenseFile != null && !businessLicenseFile.isEmpty()) {
            String licenseUrl = awsS3Service.uploadFile(businessLicenseFile);
            host.setBusinessLicense(licenseUrl);
        }

        int result = hostDao.insert(host);
        if (result > 0 && host.getHostId() != null) {
            log.info("Host registered successfully: {}", host);
            return hostDao.getHostById(host.getHostId());
        }
        // log.error("Failed to register host for userId: {}", host.getUserId()); // userId가 설정되었을 경우
        log.error("Failed to register host for userId: {} with DTO: {}", userId, registrationDto);
        throw new SQLException("Host 등록에 실패했습니다.");
    }

    /**
     * 호스트 ID로 호스트를 조회합니다.
     */
    @Override
    public Host getHostById(Long hostId) throws SQLException {
        return hostDao.getHostById(hostId);
    }

    /**
     * 모든 호스트 목록을 조회합니다.
     */
    @Override
    public List<Host> getAllHosts() throws SQLException {
        return hostDao.getHosts();
    }

    /**
     * 호스트 정보를 업데이트합니다.
     */
    @Override
    @Transactional
    public Host updateHostInfo(Long hostId, HostUpdateRequestDto updateDto) throws SQLException {
        Host existingHost = hostDao.getHostById(hostId);
        if (existingHost == null) {
            throw new SQLException("Host not found with id: " + hostId);
        }

        // 변경 가능한 필드 업데이트 (DTO에 있는 필드만 업데이트)
        if (updateDto.getBusinessName() != null) existingHost.setBusinessName(updateDto.getBusinessName());
        if (updateDto.getCeoName() != null) existingHost.setCeoName(updateDto.getCeoName());
        if (updateDto.getBusinessAddress() != null) existingHost.setBusinessAddress(updateDto.getBusinessAddress());
        if (updateDto.getBusinessPhone() != null) existingHost.setBusinessPhone(updateDto.getBusinessPhone());
        if (updateDto.getBusinessLicenseExpire() != null) existingHost.setBusinessLicenseExpire(updateDto.getBusinessLicenseExpire());
        if (updateDto.getBankName() != null) existingHost.setBankName(updateDto.getBankName());
        if (updateDto.getBankAccount() != null) existingHost.setBankAccount(updateDto.getBankAccount());
        if (updateDto.getBankOwner() != null) existingHost.setBankOwner(updateDto.getBankOwner());
        if (updateDto.getDescription() != null) existingHost.setDescription(updateDto.getDescription());
        if (updateDto.getLatitude() != null) existingHost.setLatitude(updateDto.getLatitude());
        if (updateDto.getLongitude() != null) existingHost.setLongitude(updateDto.getLongitude());
        if (updateDto.getBusinessType() != null) existingHost.setBusinessType(updateDto.getBusinessType());
        
        // 사업자 등록증 이미지 업데이트는 resubmitLicense 또는 별도 API로 처리하므로 여기서는 제외.
        // 만약 이 DTO에 이미지 파일 URL을 직접 변경하는 로직이 있다면 여기서 처리.

        existingHost.setUpdatedAt(LocalDateTime.now());
        int result = hostDao.updateHost(existingHost); // hostDao.updateHost는 Host 엔티티 전체를 업데이트
        if (result > 0) {
            log.info("Host info updated successfully: {}", existingHost);
            return hostDao.getHostById(hostId); // 변경된 정보 다시 조회
        }
        log.error("Failed to update host info for hostId: {}", hostId);
        throw new SQLException("Host 정보 업데이트에 실패했습니다.");
    }

    /**
     * 호스트를 삭제합니다.
     */
    @Override
    @Transactional
    public boolean deleteHost(Long hostId) throws SQLException {
        Host host = hostDao.getHostById(hostId);
        if (host != null && host.getBusinessLicense() != null && !host.getBusinessLicense().isEmpty()) {
            awsS3Service.deleteImage(host.getBusinessLicense());
        }
        if (host != null && host.getLicenseResubmitUrl() != null && !host.getLicenseResubmitUrl().isEmpty()) {
             awsS3Service.deleteImage(host.getLicenseResubmitUrl());
        }
        int result = hostDao.deleteHost(hostId);
        log.info("Host deleted with id: {}, result: {}", hostId, result > 0);
        return result > 0;
    }

    /**
     * 호스트 상태를 업데이트합니다.
     */
    @Override
    @Transactional
    public void updateHostStatus(Long hostId, String status, String adminComment) throws SQLException {
        Host existingHost = hostDao.getHostById(hostId);
        if (existingHost == null) {
            throw new SQLException("Host not found with id: " + hostId);
        }

        existingHost.setStatus(status);
        if (adminComment != null) {
            existingHost.setAdminComment(adminComment);
        }

        if ("ACTIVE".equals(status)) {
            existingHost.setApprovedAt(LocalDateTime.now());
            existingHost.setRejectedAt(null); // 혹시 반려 상태였다면 초기화
        } else if ("REJECT".equals(status)) {
            existingHost.setRejectedAt(LocalDateTime.now());
            existingHost.setApprovedAt(null); // 혹시 승인 상태였다면 초기화
        }
        existingHost.setUpdatedAt(LocalDateTime.now());

        // DAO의 updateHostStatus가 Host 객체를 받도록 수정하고, HostMapper.xml에서 관련 필드들을 모두 업데이트하도록 해야 함.
        // 여기서는 일단 updateHost를 호출하여 모든 변경사항을 반영한다고 가정.
        int fullUpdateResult = hostDao.updateHost(existingHost);
        if (fullUpdateResult > 0) {
             log.info("Host status updated for hostId: {} to {} with comment: {}", hostId, status, adminComment);
             // return hostDao.getHostById(hostId); // 반환 타입이 void이므로 주석 처리
        } else {
            log.error("Failed to update host status for hostId: {}", hostId);
            throw new SQLException("Host 상태 업데이트에 실패했습니다.");
        }
    }

    /**
     * 사용자 ID로 호스트를 조회합니다.
     */
    @Override
    public Host getHostByUserId(Long userId) throws SQLException {
        return hostDao.getHostByUserId(userId);
    }

    @Override
    @Transactional
    public Host resubmitLicense(Long hostId, MultipartFile licenseResubmitFile) throws SQLException, IOException {
        Host existingHost = hostDao.getHostById(hostId);
        if (existingHost == null) {
            throw new SQLException("Host not found with id: " + hostId);
        }

        if (licenseResubmitFile != null && !licenseResubmitFile.isEmpty()) {
            // 기존 재제출 이미지 삭제 로직 (선택적)
            if (existingHost.getLicenseResubmitUrl() != null && !existingHost.getLicenseResubmitUrl().isEmpty()) {
                awsS3Service.deleteImage(existingHost.getLicenseResubmitUrl());
            }
            String resubmitUrl = awsS3Service.uploadFile(licenseResubmitFile);
            existingHost.setLicenseResubmitUrl(resubmitUrl);
            existingHost.setLicenseResubmittedAt(LocalDateTime.now());
            existingHost.setStatus("WAIT"); // 재제출 시 다시 심사중 상태로 변경
            existingHost.setAdminComment("사업자 등록증 재제출됨"); // 관리자 코멘트 자동 설정
            existingHost.setUpdatedAt(LocalDateTime.now());

            int result = hostDao.updateHost(existingHost);
            if (result > 0) {
                log.info("License resubmitted for hostId: {}", hostId);
                return hostDao.getHostById(hostId);
            }
            log.error("Failed to resubmit license for hostId: {}", hostId);
            throw new SQLException("사업자 등록증 재제출에 실패했습니다.");
        }
        throw new IllegalArgumentException("재제출할 사업자 등록증 파일이 없습니다.");
    }

    @Override
    @Transactional
    public boolean updateHostStatus(Long hostId, String status) throws SQLException {
        Host existingHost = hostDao.getHostById(hostId);
        if (existingHost == null) {
            log.warn("Host not found with id: {} for status update.", hostId);
            return false;
        }

        existingHost.setStatus(status);
        existingHost.setUpdatedAt(LocalDateTime.now());

        // approvedAt, rejectedAt 등 상태 관련 시간 필드 설정
        if ("ACTIVE".equals(status) || "APPROVED".equals(status)) { // "APPROVED"도 ACTIVE와 유사하게 처리
            existingHost.setApprovedAt(LocalDateTime.now());
            existingHost.setRejectedAt(null);
            existingHost.setAdminComment(null); // 승인 시에는 이전 거절 사유/코멘트 초기화
        } else if ("REJECTED".equals(status)) {
            existingHost.setRejectedAt(LocalDateTime.now());
            existingHost.setApprovedAt(null);
            // 거절 사유는 updateHostStatusAndReason 에서 별도로 설정하므로 여기서는 건드리지 않음.
        } else if ("WAIT".equals(status) || "PENDING".equals(status)){ // PENDING도 WAIT와 유사하게 처리
            existingHost.setApprovedAt(null);
            existingHost.setRejectedAt(null);
            existingHost.setAdminComment(null);
        }


        // HostDao의 updateHostStatus는 status 문자열만 받으므로,
        // status, updatedAt, approvedAt, rejectedAt, adminComment 등을 모두 업데이트하려면
        // Host 객체 전체를 받는 updateHost 메서드를 사용해야 합니다.
        int result = hostDao.updateHost(existingHost); // Host 객체 전체를 업데이트하는 메서드로 가정
        if (result > 0) {
            log.info("Host status updated for hostId: {} to {}", hostId, status);
            return true;
        }
        log.error("Failed to update host status for hostId: {} to {}", hostId, status);
        return false;
    }

    @Override
    public List<Host> getHostsByStatus(String status) throws SQLException {
        return hostDao.getHostsByStatus(status);
    }

    @Override
    @Transactional
    public boolean updateHostStatusAndReason(Long hostId, String status, String reason) throws SQLException {
        Host existingHost = hostDao.getHostById(hostId);
        if (existingHost == null) {
            log.warn("Host not found with id: {} for status and reason update.", hostId);
            return false;
        }

        existingHost.setStatus(status);
        existingHost.setAdminComment(reason); // 거절 사유를 adminComment에 저장
        existingHost.setUpdatedAt(LocalDateTime.now());

        if ("REJECTED".equals(status)) {
            existingHost.setRejectedAt(LocalDateTime.now());
            existingHost.setApprovedAt(null);
        } else {
            // REJECTED 외의 상태로 변경하면서 사유를 남기는 경우는 현재 시나리오에 없으나,
            // 필요하다면 다른 상태 관련 시간도 여기서 설정
        }
        
        // Host 객체 전체를 업데이트
        int result = hostDao.updateHost(existingHost);
        if (result > 0) {
            log.info("Host status updated for hostId: {} to {} with reason: {}", hostId, status, reason);
            return true;
        }
        log.error("Failed to update host status for hostId: {} to {} with reason: {}", hostId, status, reason);
        return false;
    }

    @Override
    @Transactional
    public void updateHost(Host host) throws SQLException {
        // Host 엔티티에 updated_at 필드가 있다면 여기서 설정
        host.setUpdatedAt(LocalDateTime.now());
        int result = hostDao.updateHost(host);
        if (result == 0) {
            log.warn("Host update failed for hostId: {}. Maybe host not found or no changes to update.", host.getHostId());
            // 필요에 따라 예외를 던질 수 있습니다. 예를 들어, 업데이트 대상이 없거나 변경 사항이 없어도 성공으로 간주하지 않는 경우.
            // throw new SQLException("Host 업데이트에 실패했습니다. ID: " + host.getHostId());
        } else {
            log.info("Host updated successfully: {}", host);
        }
        // 인터페이스의 반환 타입이 void이므로, 별도 반환 없음.
        // 만약 Host 객체를 반환해야 한다면, 인터페이스 수정 및 아래 코드 주석 해제 필요
        // return hostDao.getHostById(host.getHostId()); 
    }

    // TODO: getHosts(Pageable pageable, String status) 구현 필요
    @Override
    public Page<Host> getHosts(Pageable pageable, String status) throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("offset", pageable.getOffset());
        params.put("limit", pageable.getPageSize());
        if (status != null && !status.isEmpty()) {
            params.put("status", status);
        }
        // Sort 정보 추가 (필요한 경우 HostMapper.xml에서 동적 정렬 처리)
        if (pageable.getSort().isSorted()) {
            // 예시: 첫 번째 정렬 조건만 사용. 다중 정렬 필요 시 로직 추가
            Sort.Order order = pageable.getSort().iterator().next();
            params.put("sortBy", order.getProperty());
            params.put("sortDirection", order.getDirection().name());
        }

        List<Host> hosts = hostDao.getHostsWithPaginationAndStatus(params);
        int total = hostDao.countHostsWithStatus(params); // status만 있는 params 또는 status 없는 전체 카운트용 params 전달

        return new PageImpl<>(hosts, pageable, total);
    }

    @Override
    public boolean checkBusinessNumberExists(String businessNumber) throws SQLException {
        Host host = hostDao.getHostByBusinessNumber(businessNumber);
        return host != null;
    }
}