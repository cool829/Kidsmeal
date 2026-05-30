package hello.example.hello_spring.member.repository;

import hello.example.hello_spring.member.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByEmailOrderByIdDesc(String email);
    void deleteAllByEmail(String email);
}