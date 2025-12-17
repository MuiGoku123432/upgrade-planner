package com.sentinovo.carbuildervin.repository.oauth;

import com.sentinovo.carbuildervin.entities.oauth.OAuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthRefreshTokenRepository extends JpaRepository<OAuthRefreshToken, UUID> {

    @Query("SELECT t FROM OAuthRefreshToken t " +
           "JOIN FETCH t.user " +
           "JOIN FETCH t.client " +
           "WHERE t.tokenHash = :tokenHash")
    Optional<OAuthRefreshToken> findByTokenHashWithUserAndClient(@Param("tokenHash") String tokenHash);

    Optional<OAuthRefreshToken> findByTokenHash(String tokenHash);

    @Query("SELECT t FROM OAuthRefreshToken t " +
           "WHERE t.tokenHash = :tokenHash " +
           "AND t.isRevoked = false " +
           "AND t.expiresAt > :now")
    Optional<OAuthRefreshToken> findValidByTokenHash(
            @Param("tokenHash") String tokenHash,
            @Param("now") OffsetDateTime now);

    @Query("SELECT t FROM OAuthRefreshToken t " +
           "JOIN FETCH t.client " +
           "WHERE t.user.id = :userId AND t.isRevoked = false")
    List<OAuthRefreshToken> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT t FROM OAuthRefreshToken t WHERE t.user.id = :userId")
    List<OAuthRefreshToken> findByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE OAuthRefreshToken t SET t.isRevoked = true " +
           "WHERE t.user.id = :userId AND t.client.id = :clientId")
    void revokeByUserIdAndClientId(
            @Param("userId") UUID userId,
            @Param("clientId") UUID clientId);

    @Modifying
    @Query("UPDATE OAuthRefreshToken t SET t.isRevoked = true WHERE t.user.id = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM OAuthRefreshToken t WHERE t.expiresAt < :now")
    int deleteExpired(@Param("now") OffsetDateTime now);

    @Modifying
    @Query("DELETE FROM OAuthRefreshToken t WHERE t.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}
