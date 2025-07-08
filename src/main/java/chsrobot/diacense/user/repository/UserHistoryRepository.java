package chsrobot.diacense.user.repository;

import chsrobot.diacense.user.model.User;
import chsrobot.diacense.user.model.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    Optional<UserHistory> findByUserId(Long userId);

    List<UserHistory> findByUserAndCreatedAtAfter(User user, LocalDateTime dateTime);
}
