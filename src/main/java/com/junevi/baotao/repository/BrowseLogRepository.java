package com.junevi.baotao.repository;

import com.junevi.baotao.domain.BrowseLog;
import com.junevi.baotao.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrowseLogRepository extends JpaRepository<BrowseLog, Long> {

    List<BrowseLog> findByUser(User user);
}


