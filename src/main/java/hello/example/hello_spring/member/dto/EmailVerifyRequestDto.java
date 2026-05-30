package hello.example.hello_spring.member.dto;

import lombok.Getter;

@Getter
public class EmailVerifyRequestDto {
    private String email;
    private String code;
}