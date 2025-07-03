package chsrobot.diacense.user;

import chsrobot.diacense.user.model.User;
import chsrobot.diacense.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(UserDto userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        currentUser.setFirstName(userDto.getFirstName());
        currentUser.setLastName(userDto.getLastName());
        currentUser.setPhoneNumber(userDto.getPhoneNumber());
        currentUser.setBirthDate(userDto.getBirthDate());
        currentUser.setMaritalStatus(userDto.getMaritalStatus());
        currentUser.setGender(userDto.getGender());
        currentUser.setAge(userDto.getAge());
        currentUser.setHeight(userDto.getHeight());
        currentUser.setWeight(userDto.getWeight());
        currentUser.setDiabetes(userDto.isDiabetes());
        return userRepository.save(currentUser);
    }
}
