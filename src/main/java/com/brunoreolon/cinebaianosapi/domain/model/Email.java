package com.brunoreolon.cinebaianosapi.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Email {

    private String to;
    private String subject;
    private String content;

}
