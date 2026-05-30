package com.docesforg.bura.server.signal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RadioSignalTestRepository extends JpaRepository<RadioSignalTestEntity, Long> {
    List<RadioSignalTestEntity> findAllByAccountIdOrderByCreatedAtDesc(long accountId);
}
