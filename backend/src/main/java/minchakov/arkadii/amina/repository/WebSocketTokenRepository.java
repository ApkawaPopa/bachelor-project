package minchakov.arkadii.amina.repository;

import minchakov.arkadii.amina.model.WebSocketToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebSocketTokenRepository extends JpaRepository<WebSocketToken, UUID> {
    Optional<WebSocketToken> getWebSocketTokenById(UUID id);

    @Modifying
    @Query(value = "DELETE FROM web_socket_token WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Integer userId);
}
