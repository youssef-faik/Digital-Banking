package com.example.digitalbanking.dtos;

import lombok.Data;
import java.util.List;

@Data
public class AppUserDTO {
    private Long userId;
    private String username;
    private String email;
    private List<String> roles; // Representing roles by their names
}
