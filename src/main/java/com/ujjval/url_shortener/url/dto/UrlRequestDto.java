package com.ujjval.url_shortener.url.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UrlRequestDto {

    @NotBlank(message = "URL cannot be blank")
    @Pattern(
            regexp = "^(http://|https://).+",
            message = "URL must start with http:// or https://"
    )
    private String originalUrl;

    private Long expiresInSeconds;
}