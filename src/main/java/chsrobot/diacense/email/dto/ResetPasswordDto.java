package chsrobot.diacense.email.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDto {
    private String email;
    private String verificationCode;
    private String newPassword;
}
