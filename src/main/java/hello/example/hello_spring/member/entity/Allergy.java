package hello.example.hello_spring.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allergies")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String allergyName;
}