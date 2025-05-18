package com.ssafy.trip.host;

import com.ssafy.trip.user.UserDao;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 호스트 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HostServiceImpl implements HostService {

    private final UserDao userDao;
    private final HostDao dao;

    /**
     * 새 호스트를 등록합니다.
     */
    @Override
    @Transactional
    public int registHost(Host host, Long userId) throws SQLException {
        userDao.updateUserRole(userId, host.getRole());
        log.info("Register host: " + host);
        log.info("User id: " + userId);
        return dao.insert(host, userId);
    }

    /**
     * 호스트 ID로 호스트를 조회합니다.
     */
    @Override
    public Host getHostById(Long hostId) throws SQLException {
        return dao.getHostById(hostId);
    }

    /**
     * 모든 호스트 목록을 조회합니다.
     */
    @Override
    public List<Host> getHostList() throws SQLException {
        return dao.getHosts();
    }

    /**
     * 호스트 정보를 업데이트합니다.
     */
    @Override
    public int updateHost(Host host) throws SQLException {
        return dao.updateHost(host);
    }

    /**
     * 호스트를 삭제합니다.
     */
    @Override
    public int deleteHost(Long hostId) throws SQLException {
        return dao.deleteHost(hostId);
    }

    /**
     * 호스트 상태를 업데이트합니다.
     */
    @Override
    public int updateHostStatus(Long hostId, String hostStatus) throws SQLException {
        return dao.updateHostStatus(hostId, hostStatus);
    }

    /**
     * 사용자 ID로 호스트를 조회합니다.
     */
    @Override
    public Host getHostByUserId(Long userId) throws SQLException {
        return dao.getHostByUserId(userId);
    }
}