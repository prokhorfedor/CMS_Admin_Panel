package com.kpi.web.systems.lab3.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminPanelPageDto {

    private String header;

    private String content;

    private String footer;
}
