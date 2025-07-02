package chsrobot.diacense.security;

import chsrobot.diacense.email.dto.ResetPasswordDto;
import chsrobot.diacense.email.dto.SendOtpByEmailDto;
import chsrobot.diacense.email.dto.VerifyUserDto;
import chsrobot.diacense.security.dto.LoginResponse;
import chsrobot.diacense.security.dto.LoginUserDto;
import chsrobot.diacense.security.dto.RegisterUserDto;
import chsrobot.diacense.user.model.User;
import chsrobot.diacense.user.model.UserVerification;
import chsrobot.diacense.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email Is Already In Use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        else if (userRepository.findByPhoneNumber(registerUserDto.getPhoneNumber()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Phone Number Is Already In Use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        else if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Username Is Already In Use");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        try {
            authenticationService.register(registerUserDto);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Created");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Something Went Wrong");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        if (userRepository.findByEmail(loginUserDto.getUsername()).get().isEnabled()) {
            try {
                User authenticatedUser = authenticationService.authenticate(loginUserDto);

                String jwtToken = jwtService.generateToken(authenticatedUser);

                LoginResponse loginResponse =
                        new LoginResponse().setToken(jwtToken).setExpiresIn(jwtService.getExpirationTime());

                return ResponseEntity.ok(loginResponse);
            } catch (RuntimeException e) {
                if (userRepository.findByEmail(loginUserDto.getUsername()).isPresent()) {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Invalid Password");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                } else {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Something Went Wrong");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
        } else if (userRepository.findByEmail(loginUserDto.getUsername()).isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Username Not Found");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        } else if (userRepository.findByEmail(loginUserDto.getUsername()).get().isEnabled()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User Not Verified");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Something Went Wrong");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        User user = userRepository.findByEmail(verifyUserDto.getEmail()).orElse(null);
        UserVerification userVerification = user != null ? user.getUserVerification() : null;
        if (user == null || userVerification == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (userRepository.findByEmail(verifyUserDto.getEmail()).get().isEnabled()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User Already Verified");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else if (userVerification.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Verification Code Expired");
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
        } else if (!userVerification.getVerificationCode().equals(verifyUserDto.getVerificationCode())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid Verification Code");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (userRepository.findByEmail(verifyUserDto.getEmail()).isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            try {
                authenticationService.verifyUser(verifyUserDto);
                Map<String, String> response = new HashMap<>();
                response.put("message", "User Verified");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Something Went Wrong");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestBody SendOtpByEmailDto sendOtpByEmailDto) {
        if (userRepository.findByEmail(sendOtpByEmailDto.getEmail()).isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (userRepository.findByEmail(sendOtpByEmailDto.getEmail()).get().isEnabled()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User Already Verified");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            try {
                authenticationService.resendVerificationCode(sendOtpByEmailDto.getEmail());
                Map<String, String> response = new HashMap<>();
                response.put("message", "Verification Code Sent");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Something Went Wrong");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody SendOtpByEmailDto sendOtpByEmailDto) {
        if (userRepository.findByEmail(sendOtpByEmailDto.getEmail()).isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            try {
                authenticationService.sendPasswordResetEmailOtp(
                        userRepository.findByEmail(sendOtpByEmailDto.getEmail()).get());
                Map<String, String> response = new HashMap<>();
                response.put("message", "Reset Password Email Sent");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Something Went Wrong");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        User user = userRepository.findByEmail(resetPasswordDto.getEmail()).orElse(null);
        UserVerification userVerification = user != null ? user.getUserVerification() : null;
        if (userRepository.findByEmail(resetPasswordDto.getEmail()).isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (userVerification.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Reset Password Code Expired");
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
        } else if (!userVerification.getVerificationCode().equals(resetPasswordDto.getVerificationCode())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invalid Reset Password Code");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            try {
                authenticationService.resetPassword(resetPasswordDto);
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password Reset");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Something Went Wrong");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }
}
