package com.sentinovo.carbuildervin.repository.oauth;

import com.sentinovo.carbuildervin.entities.oauth.OAuthClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OAuthClientRepository extends JpaRepository<OAuthClient, UUID> {

    Optional<OAuthClient> findByClientId(String clientId);

    @Query("SELECT c FROM OAuthClient c WHERE c.clientId = :clientId AND c.isActive = true")
    Optional<OAuthClient> findByClientIdAndActive(@Param("clientId") String clientId);

    @Query("SELECT c FROM OAuthClient c WHERE c.isActive = true")
    List<OAuthClient> findAllActive();

    boolean existsByClientId(String clientId);
}
