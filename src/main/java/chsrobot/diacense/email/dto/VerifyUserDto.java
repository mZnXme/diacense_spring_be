package chsrobot.diacense.email.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyUserDto {
    private String Email;
    private String verificationCode;
}
