package hello.example.hello_spring.member.dto;

import lombok.Getter;

@Getter
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
}