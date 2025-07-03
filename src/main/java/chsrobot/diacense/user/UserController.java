package chsrobot.diacense.user;

import chsrobot.diacense.user.model.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> userProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(currentUser);
    }

    @PostMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserDto userDto) {
        userService.updateUser(userDto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "อัปเดตข้อมูลผู้ใช้สำเร็จ");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
