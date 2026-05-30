package hello.example.hello_spring.member.service;

import hello.example.hello_spring.member.entity.EmailVerification;
import hello.example.hello_spring.member.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    @Transactional
    public void sendVerificationCode(String email) {
        emailVerificationRepository.deleteAllByEmail(email);

        String code = String.format("%04d", new Random().nextInt(10000));

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();
        emailVerificationRepository.save(verification);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[KidsMeal Pro] 이메일 인증 코드");
        message.setText("인증 코드: " + code + "\n\n5분 안에 입력해주세요.");
        mailSender.send(message);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new RuntimeException("인증 코드를 먼저 요청해주세요."));

        if (verification.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("인증 코드가 만료되었습니다.");
        }

        if (!verification.getCode().equals(code)) {
            throw new RuntimeException("인증 코드가 일치하지 않습니다.");
        }

        verification.verify();
    }
}