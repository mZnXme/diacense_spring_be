package chsrobot.diacense.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserDto {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthDate;
    private String maritalStatus;
    private String gender;
    private int age;
    private double height;
    private double weight;

    @JsonProperty("isDiabetes")
    private boolean diabetes;
}
