package com.xreadup.ai.userservice.controller;

import com.xreadup.ai.userservice.dto.VerifyEmailRequest;
import com.xreadup.ai.userservice.service.EmailService;
import com.xreadup.ai.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 邮箱验证控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "邮箱验证", description = "用户邮箱验证相关接口")
public class EmailController {

    private final EmailService emailService;
    private final UserService userService;

    /**
     * 验证邮箱
     */
    @PostMapping("/verify-email")
    @Operation(summary = "验证邮箱", description = "使用验证码验证用户邮箱")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        try {
            boolean verified = emailService.verifyEmail(request.getEmail(), request.getCode());
            
            if (verified) {
                userService.activateUser(request.getEmail());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "邮箱验证成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "验证码无效或已过期"
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 重新发送验证邮件
     */
    @PostMapping("/resend-verification")
    @Operation(summary = "重新发送验证邮件", description = "重新发送邮箱验证邮件")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (emailService.hasPendingVerification(email)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "验证邮件已发送，请15分钟后再试"
                ));
            }
            
            var user = userService.findByEmail(email);
            if (user == null || user.getEmailVerified()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户不存在或已验证"
                ));
            }
            
            emailService.sendVerificationEmail(email, user.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "验证邮件已重新发送"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}