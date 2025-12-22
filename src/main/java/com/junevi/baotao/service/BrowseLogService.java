package com.junevi.baotao.service;

import com.junevi.baotao.domain.BrowseAction;
import com.junevi.baotao.domain.BrowseLog;
import com.junevi.baotao.domain.Product;
import com.junevi.baotao.domain.User;
import com.junevi.baotao.repository.BrowseLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BrowseLogService {

    private final BrowseLogRepository browseLogRepository;

    public BrowseLogService(BrowseLogRepository browseLogRepository) {
        this.browseLogRepository = browseLogRepository;
    }

    @Transactional
    public void logView(User user, Product product) {
        if (user == null) {
            return;
        }
        BrowseLog log = new BrowseLog();
        log.setUser(user);
        log.setProduct(product);
        log.setAction(BrowseAction.VIEW);
        browseLogRepository.save(log);
    }

    @Transactional
    public void logBuy(User user, Product product) {
        if (user == null) {
            return;
        }
        BrowseLog log = new BrowseLog();
        log.setUser(user);
        log.setProduct(product);
        log.setAction(BrowseAction.BUY);
        browseLogRepository.save(log);
    }

    public List<BrowseLog> listLogsForUser(User user) {
        return browseLogRepository.findByUser(user);
    }
}


