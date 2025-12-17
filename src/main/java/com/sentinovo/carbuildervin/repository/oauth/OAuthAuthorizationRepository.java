package com.sentinovo.carbuildervin.repository.oauth;

import com.sentinovo.carbuildervin.entities.oauth.OAuthAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthAuthorizationRepository extends JpaRepository<OAuthAuthorization, UUID> {

    @Query("SELECT a FROM OAuthAuthorization a WHERE a.user.id = :userId AND a.client.id = :clientId")
    Optional<OAuthAuthorization> findByUserIdAndClientId(
            @Param("userId") UUID userId,
            @Param("clientId") UUID clientId);

    @Query("SELECT a FROM OAuthAuthorization a JOIN FETCH a.client WHERE a.user.id = :userId")
    List<OAuthAuthorization> findByUserIdWithClient(@Param("userId") UUID userId);

    @Query("SELECT a FROM OAuthAuthorization a WHERE a.user.id = :userId")
    List<OAuthAuthorization> findByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM OAuthAuthorization a WHERE a.user.id = :userId AND a.client.id = :clientId")
    void deleteByUserIdAndClientId(
            @Param("userId") UUID userId,
            @Param("clientId") UUID clientId);

    @Modifying
    @Query("DELETE FROM OAuthAuthorization a WHERE a.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    boolean existsByUserIdAndClientId(UUID userId, UUID clientId);
}
