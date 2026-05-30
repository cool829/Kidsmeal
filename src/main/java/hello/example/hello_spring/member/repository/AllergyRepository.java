package hello.example.hello_spring.member.repository;

import hello.example.hello_spring.member.entity.Allergy;
import hello.example.hello_spring.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllergyRepository extends JpaRepository<Allergy, Long> {
    List<Allergy> findAllByMember(Member member);
    void deleteAllByMember(Member member);
}