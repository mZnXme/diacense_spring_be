package chsrobot.diacense.security.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterUserDto {
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String password;
}
