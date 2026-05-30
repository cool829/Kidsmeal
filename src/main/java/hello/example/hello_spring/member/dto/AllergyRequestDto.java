package hello.example.hello_spring.member.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class AllergyRequestDto {
    private List<String> allergies;
}