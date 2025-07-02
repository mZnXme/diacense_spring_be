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
        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "ชื่อผู้ใช้นี้ถูกใช้แล้ว");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        else if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "อีเมลนี้ถูกใช้แล้ว");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        else if (userRepository.findByPhoneNumber(registerUserDto.getPhoneNumber()).isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "เบอร์โทรศัพท์นี้ถูกใช้แล้ว");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        try {
            authenticationService.register(registerUserDto);

            Map<String, String> response = new HashMap<>();
            response.put("message", "สร้างบัญชีผู้ใช้สำเร็จแล้ว");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "เกิดข้อผิดพลาดในการลงทะเบียนผู้ใช้");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        if (userRepository.findByUsername(loginUserDto.getUsername()).isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "ไม่พบผู้ใช้ดังกล่าว");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (!userRepository.findByUsername(loginUserDto.getUsername()).get().isEnabled()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "ผู้ใช้ยังไม่ได้รับการยืนยันอีเมล");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        if (userRepository.findByUsername(loginUserDto.getUsername()).get().isEnabled()) {
            try {
                User authenticatedUser = authenticationService.authenticate(loginUserDto);

                String jwtToken = jwtService.generateToken(authenticatedUser);

                LoginResponse loginResponse =
                        new LoginResponse().setToken(jwtToken).setExpiresIn(jwtService.getExpirationTime());

                return ResponseEntity.ok(loginResponse);
            } catch (RuntimeException e) {
                if (userRepository.findByUsername(loginUserDto.getUsername()).isPresent()) {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                } else {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "เกิดข้อผิดพลาดในการเข้าสู่ระบบ");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "เกิดข้อผิดพลาดในการเข้าสู่ระบบ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        User user = userRepository.findByEmail(verifyUserDto.getEmail()).orElse(null);
        UserVerification userVerification = user != null ? user.getUserVerification() : null;
        if (user == null || userVerification == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "ไม่พบผู้ใช้ดังกล่าว");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (userRepository.findByEmail(verifyUserDto.getEmail()).get().isEnabled()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "ผู้ใช้ได้รับการยืนยันอีเมลแล้ว");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else if (userVerification.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "รหัสยืนยันอีเมลหมดอายุแล้ว");
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
        } else if (!userVerification.getVerificationCode().equals(verifyUserDto.getVerificationCode())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "รหัสยืนยันอีเมลไม่ถูกต้อง");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else if (userRepository.findByEmail(verifyUserDto.getEmail()).isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "ไม่พบอีเมลดังกล่าว");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            try {
                authenticationService.verifyUser(verifyUserDto);
                Map<String, String> response = new HashMap<>();
                response.put("message", "ยืนยันผู้ใช้สำเร็จแล้ว");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "เกิดข้อผิดพลาดในการยืนยันผู้ใช้");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestBody SendOtpByEmailDto sendOtpByEmailDto) {
        if (userRepository.findByEmail(sendOtpByEmailDto.getEmail()).isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "ไม่พบอีเมลดังกล่าว");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (userRepository.findByEmail(sendOtpByEmailDto.getEmail()).get().isEnabled()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "ผู้ใช้ได้รับการยืนยันอีเมลแล้ว");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            try {
                authenticationService.resendVerificationCode(sendOtpByEmailDto.getEmail());
                Map<String, String> response = new HashMap<>();
                response.put("message", "รหัสยืนยันอีเมลถูกส่งใหม่แล้ว");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "เกิดข้อผิดพลาดในการส่งรหัสยืนยันอีเมล");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody SendOtpByEmailDto sendOtpByEmailDto) {
        if (userRepository.findByEmail(sendOtpByEmailDto.getEmail()).isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "ไม่พบอีเมลดังกล่าว");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            try {
                authenticationService.sendPasswordResetEmailOtp(
                        userRepository.findByEmail(sendOtpByEmailDto.getEmail()).get());
                Map<String, String> response = new HashMap<>();
                response.put("message", "รหัสรีเซ็ตรหัสผ่านถูกส่งไปยังอีเมลแล้ว");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "เกิดข้อผิดพลาดในการส่งรหัสรีเซ็ตรหัสผ่าน");
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
            response.put("message", "ไม่พบอีเมลดังกล่าว");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (userVerification.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "รหัสรีเซ็ตรหัสผ่านหมดอายุแล้ว");
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
        } else if (!userVerification.getVerificationCode().equals(resetPasswordDto.getVerificationCode())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "รหัสรีเซ็ตรหัสผ่านไม่ถูกต้อง");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            try {
                authenticationService.resetPassword(resetPasswordDto);
                Map<String, String> response = new HashMap<>();
                response.put("message", "รีเซ็ตรหัสผ่านสำเร็จแล้ว");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "เกิดข้อผิดพลาดในการรีเซ็ตรหัสผ่าน");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }
}
