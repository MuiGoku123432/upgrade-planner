package com.sentinovo.carbuildervin.repository.oauth;

import com.sentinovo.carbuildervin.entities.oauth.OAuthAuthorizationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthAuthorizationCodeRepository extends JpaRepository<OAuthAuthorizationCode, UUID> {

    @Query("SELECT c FROM OAuthAuthorizationCode c " +
           "JOIN FETCH c.user " +
           "JOIN FETCH c.client " +
           "WHERE c.codeHash = :codeHash")
    Optional<OAuthAuthorizationCode> findByCodeHashWithUserAndClient(@Param("codeHash") String codeHash);

    Optional<OAuthAuthorizationCode> findByCodeHash(String codeHash);

    @Query("SELECT c FROM OAuthAuthorizationCode c " +
           "WHERE c.codeHash = :codeHash AND c.used = false AND c.expiresAt > :now")
    Optional<OAuthAuthorizationCode> findValidByCodeHash(
            @Param("codeHash") String codeHash,
            @Param("now") OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM OAuthAuthorizationCode c WHERE c.expiresAt < :now")
    int deleteExpired(@Param("now") OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM OAuthAuthorizationCode c WHERE c.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}
