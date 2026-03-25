package com.brunoreolon.cinebaianosapi.domain.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Email {

    private String to;
    private String subject;
    private String content;

}