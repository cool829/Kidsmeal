package hello.example.hello_spring.member.service;

import hello.example.hello_spring.config.JwtTokenProvider;
import hello.example.hello_spring.member.dto.*;
import hello.example.hello_spring.member.entity.Allergy;
import hello.example.hello_spring.member.entity.EmailVerification;
import hello.example.hello_spring.member.entity.Member;
import hello.example.hello_spring.member.entity.RefreshToken;
import hello.example.hello_spring.member.repository.AllergyRepository;
import hello.example.hello_spring.member.repository.EmailVerificationRepository;
import hello.example.hello_spring.member.repository.MemberRepository;
import hello.example.hello_spring.member.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final AllergyRepository allergyRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public void signup(SignupRequestDto request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        EmailVerification verification = emailVerificationRepository
                .findTopByEmailOrderByIdDesc(request.getEmail())
                .orElseThrow(() -> new RuntimeException("이메일 인증이 필요합니다."));

        if (!verification.isVerified()) {
            throw new RuntimeException("이메일 인증을 완료해주세요.");
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();
        memberRepository.save(member);
        emailVerificationRepository.deleteAllByEmail(request.getEmail());
    }

    // 로그인
    public TokenDto login(LoginRequestDto request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        // Refresh Token DB 저장
        refreshTokenRepository.findByEmail(member.getEmail())
                .ifPresentOrElse(
                        rt -> rt.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .email(member.getEmail())
                                        .token(refreshToken)
                                        .build()
                        )
                );

        return new TokenDto(accessToken, refreshToken);
    }

    // 토큰 재발급
    public TokenDto reissue(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        String email = jwtTokenProvider.getEmail(refreshToken);

        RefreshToken saved = refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("로그아웃된 사용자입니다."));

        if (!saved.getToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh Token이 일치하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

        saved.updateToken(newRefreshToken);

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    // 알러지 등록/수정
    @Transactional
    public void updateAllergies(String email, AllergyRequestDto request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        allergyRepository.deleteAllByMember(member);

        List<Allergy> allergies = request.getAllergies().stream()
                .map(name -> Allergy.builder()
                        .member(member)
                        .allergyName(name)
                        .build())
                .collect(Collectors.toList());

        allergyRepository.saveAll(allergies);
    }

    // 알러지 조회
    public List<String> getAllergies(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return allergyRepository.findAllByMember(member).stream()
                .map(Allergy::getAllergyName)
                .collect(Collectors.toList());
    }

    // 프로필 조회
    public Member getProfile(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
    }

    // 프로필 수정
    @Transactional
    public void updateProfile(String email, ProfileUpdateRequestDto request) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        if (request.getName() != null) {
            member.updateProfile(request.getName());
        }
        if (request.getPassword() != null) {
            member.updatePassword(passwordEncoder.encode(request.getPassword()));
        }
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMember(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        allergyRepository.deleteAllByMember(member);
        refreshTokenRepository.deleteByEmail(email);
        memberRepository.delete(member);
    }
}