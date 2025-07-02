package chsrobot.diacense.security;

import chsrobot.diacense.email.EmailService;
import chsrobot.diacense.email.dto.ResetPasswordDto;
import chsrobot.diacense.email.dto.VerifyUserDto;
import chsrobot.diacense.security.dto.LoginUserDto;
import chsrobot.diacense.security.dto.RegisterUserDto;
import chsrobot.diacense.user.model.Role;
import chsrobot.diacense.user.model.User;
import chsrobot.diacense.user.model.UserVerification;
import chsrobot.diacense.user.UserRepository;
import chsrobot.diacense.user.UserVerificationRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final UserVerificationRepository userVerificationRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository, UserVerificationRepository userVerificationRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userVerificationRepository = userVerificationRepository;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User register(RegisterUserDto input) {
        User user = new User();
        user.setUsername(input.getUsername());
        user.setFirstName(input.getFirstName());
        user.setLastName(input.getLastName());
        user.setEmail(input.getEmail());
        user.setPhoneNumber(input.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(false);

        UserVerification verification = UserVerification.builder()
                .verificationCode(generateVerificationCode())
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .user(user)
                .build();

        user.setUserVerification(verification);

        userRepository.save(user);

        sendVerificationEmail(user);

        System.out.println("User registered: " + user.getUsername());
        return user;
    }

    public User authenticate(LoginUserDto input) {
        if (userRepository.findByUsername(input.getUsername()).get().isEnabled()) {;
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(input.getUsername(), input.getPassword()));

            return userRepository
                    .findByUsername(input.getUsername())
                    .orElseThrow(/*() -> new RuntimeException("User not found")*/ );
        }
        else {
            throw new RuntimeException();
        }
    }

    public void verifyUser(VerifyUserDto input) {
        User user =
                userRepository
                        .findByEmail(input.getEmail())
                        .orElseThrow(/*() -> new RuntimeException("User not found")*/ );

        UserVerification userVerification = user.getUserVerification();

        if (userVerification.getVerificationCode().equals(input.getVerificationCode())
                && userVerification.getExpiryDate().isAfter(LocalDateTime.now())) {
            user.setEnabled(true);
            userVerification.setVerificationCode(null);
            userVerification.setExpiryDate(null);
            userVerificationRepository.save(userVerification);
            System.out.println("User verified: " + user.getUsername());
        }
    }

    public void resendVerificationCode(String email) {
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(/*() -> new RuntimeException("User not found")*/ );

        UserVerification userVerification = user.getUserVerification();
        userVerification.setVerificationCode(generateVerificationCode());
        userVerification.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        userVerificationRepository.save(userVerification);
        sendVerificationEmail(user);
    }

    public void sendVerificationEmail(User user) {
        UserVerification userVerification = user.getUserVerification();
        System.out.println("Sending verification email to: " + user.getEmail());
        String subject = "Verify your email";
        String verificationCode = "VERIFICATION CODE " + userVerification.getVerificationCode();
        String htmlMessage =
                "<html>"
                        + "<body style=\"font-family: Arial, sans-serif;\">"
                        + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                        + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                        + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                        + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                        + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                        + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">"
                        + verificationCode
                        + "</p>"
                        + "</div>"
                        + "</div>"
                        + "</body>"
                        + "</html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
            System.out.println("Success sending verification email to: " + user.getEmail());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email");
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(200001) + 400000;
        return String.format("%06d", code);
    }

    public void sendPasswordResetEmailOtp(User user) {
        UserVerification userVerification = user.getUserVerification();
        userVerification.setVerificationCode(generateVerificationCode());
        userVerification.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        String subject = "Reset your password";
        String verificationCode = "VERIFICATION CODE " + userVerification.getVerificationCode();
        String htmlMessage =
                "<html>"
                        + "<body style=\"font-family: Arial, sans-serif;\">"
                        + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                        + "<h2 style=\"color: #333;\">Reset your password</h2>"
                        + "<p style=\"font-size: 16px;\">Please enter the verification code below to reset your password:</p>"
                        + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                        + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                        + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">"
                        + verificationCode
                        + "</p>"
                        + "</div>"
                        + "</div>"
                        + "</body>"
                        + "</html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email");
        }
    }

    public void resetPassword(ResetPasswordDto input) {
        User user =
                userRepository
                        .findByEmail(input.getEmail())
                        .orElseThrow(/*() -> new RuntimeException("User not found")*/ );
        UserVerification userVerification = user.getUserVerification();
        if (userVerification.getVerificationCode().equals(input.getVerificationCode())
                && userVerification.getExpiryDate().isAfter(LocalDateTime.now())) {
            user.setPassword(passwordEncoder.encode(input.getNewPassword()));
            user.setEnabled(true);
            userVerification.setVerificationCode(null);
            userVerification.setExpiryDate(null);
            userRepository.save(user);
        }
    }
}