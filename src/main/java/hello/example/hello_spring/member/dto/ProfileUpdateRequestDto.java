package hello.example.hello_spring.member.dto;

import lombok.Getter;

@Getter
public class ProfileUpdateRequestDto {
    private String name;
    private String password;
}