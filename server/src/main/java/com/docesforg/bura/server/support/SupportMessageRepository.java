package com.docesforg.bura.server.support;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SupportMessageRepository extends JpaRepository<SupportMessageEntity, Long> {
    List<SupportMessageEntity> findAllByAccountIdOrderByCreatedAtAsc(long accountId);

    Optional<SupportMessageEntity> findFirstByAccountIdOrderByCreatedAtAsc(long accountId);

    Optional<SupportMessageEntity> findFirstByAccountIdOrderByCreatedAtDesc(long accountId);

    List<SupportMessageEntity> findAllByOrderByCreatedAtDesc();

    List<SupportMessageEntity> findAllByAccountIdAndSenderAndSeenByAdminFalse(long accountId, String sender);

    boolean existsByAccountId(long accountId);

    boolean existsByAccountIdAndSenderAndSeenByAdminFalse(long accountId, String sender);

    long countByAccountId(long accountId);

    void deleteAllByAccountId(long accountId);

    @Query("select count(distinct m.accountId) from SupportMessageEntity m")
    long countDistinctAccountId();
}
