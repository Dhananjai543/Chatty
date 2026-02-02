package com.chatty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinByCodeRequest {

    @NotBlank(message = "Secret code is required")
    @Size(min = 8, max = 8, message = "Secret code must be 8 characters")
    private String secretCode;
}
