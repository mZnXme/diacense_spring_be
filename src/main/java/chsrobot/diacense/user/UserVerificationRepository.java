package chsrobot.diacense.user;

import chsrobot.diacense.user.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
    UserVerification findByUserId(Long userId);
}