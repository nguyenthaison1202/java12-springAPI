package com.group1.MockProject.repository;

import com.group1.MockProject.entity.ConfirmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface ConfirmTokenRepository extends JpaRepository<ConfirmToken, Integer> {
    Optional<ConfirmToken> findByToken(String token);

    @Transactional
    @Modifying
    @Query("UPDATE ConfirmToken c " + "SET c.confirmedAt = ?2 " + "WHERE c.token = ?1")
    int updateConfirmedAt(String token, LocalDateTime confirmedAt);
}
