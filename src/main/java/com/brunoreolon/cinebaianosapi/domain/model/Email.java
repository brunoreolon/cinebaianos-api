package com.brunoreolon.cinebaianosapi.domain.model;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Email {

    private String to;
    private String subject;
    private String content;

}