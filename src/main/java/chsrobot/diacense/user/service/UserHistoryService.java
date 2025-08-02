package chsrobot.diacense.user.service;

import chsrobot.diacense.user.EspHistoryDto;
import chsrobot.diacense.user.UserHistoryDto;
import chsrobot.diacense.user.repository.UserHistoryRepository;
import chsrobot.diacense.user.model.User;
import chsrobot.diacense.user.model.UserHistory;

import chsrobot.diacense.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UserHistoryService {
    private final UserHistoryRepository userHistoryRepository;
    private final UserRepository userRepository;

    public UserHistory createNewHistory(UserHistoryDto userHistoryDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        UserHistory userHistory = new UserHistory();
        userHistory.setType(userHistoryDto.getType());
        userHistory.setValue(userHistoryDto.getValue());
        userHistory.setUser(currentUser);
        return userHistoryRepository.save(userHistory);
    }

    public List<UserHistory> getUserHistory(int days) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return userHistoryRepository.findByUserAndCreatedAtAfter(currentUser, fromDate);
    }

    public UserHistory getUserHistoryByDesc() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        return userHistoryRepository.findTopByUserOrderByCreatedAtDesc(currentUser)
                .orElseThrow(() -> new RuntimeException("No history found for user"));
    }

    public UserHistory addNewHistoryByEsp(EspHistoryDto espHistoryDto) {
        User currentUser = userRepository.findByUsername(espHistoryDto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserHistory userHistory = new UserHistory();
        userHistory.setType(espHistoryDto.getType());
        userHistory.setValue(espHistoryDto.getValue());
        userHistory.setUser(currentUser);
        return userHistoryRepository.save(userHistory);
    }
}
