package hello.example.hello_spring.member.controller;

import hello.example.hello_spring.member.dto.*;
import hello.example.hello_spring.member.entity.Member;
import hello.example.hello_spring.member.service.EmailService;
import hello.example.hello_spring.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;

    // 인증 코드 전송
    @PostMapping("/auth/email/send")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request) {
        emailService.sendVerificationCode(request.get("email"));
        return ResponseEntity.ok(Map.of("result", "success", "message", "인증 코드가 전송되었습니다."));
    }

    // 인증 코드 확인
    @PostMapping("/auth/email/verify")
    public ResponseEntity<?> verifyCode(@RequestBody EmailVerifyRequestDto request) {
        emailService.verifyCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("result", "success", "message", "인증이 완료되었습니다."));
    }

    // 회원가입
    @PostMapping("/auth/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequestDto request) {
        memberService.signup(request);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        TokenDto token = memberService.login(request);
        return ResponseEntity.ok(Map.of(
                "result", "success",
                "accessToken", token.getAccessToken(),
                "refreshToken", token.getRefreshToken()
        ));
    }

    // 알러지 등록/수정
    @PostMapping("/members/allergies")
    public ResponseEntity<?> updateAllergies(@AuthenticationPrincipal String email,
                                             @RequestBody AllergyRequestDto request) {
        memberService.updateAllergies(email, request);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 알러지 조회
    @GetMapping("/members/allergies")
    public ResponseEntity<?> getAllergies(@AuthenticationPrincipal String email) {
        List<String> allergies = memberService.getAllergies(email);
        return ResponseEntity.ok(Map.of("result", "success", "allergies", allergies));
    }

    // 프로필 조회
    @GetMapping("/members/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal String email) {
        Member member = memberService.getProfile(email);
        return ResponseEntity.ok(Map.of(
                "result", "success",
                "email", member.getEmail(),
                "name", member.getName()
        ));
    }

    // 프로필 수정
    @PutMapping("/members/me")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal String email,
                                           @RequestBody ProfileUpdateRequestDto request) {
        memberService.updateProfile(email, request);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 회원 탈퇴
    @DeleteMapping("/members/me")
    public ResponseEntity<?> deleteMember(@AuthenticationPrincipal String email) {
        memberService.deleteMember(email);
        return ResponseEntity.ok(Map.of("result", "success"));
    }

    // 토큰 재발급
    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissue(@RequestBody Map<String, String> request) {
        TokenDto token = memberService.reissue(request.get("refreshToken"));
        return ResponseEntity.ok(Map.of(
                "result", "success",
                "accessToken", token.getAccessToken(),
                "refreshToken", token.getRefreshToken()
        ));
    }
}