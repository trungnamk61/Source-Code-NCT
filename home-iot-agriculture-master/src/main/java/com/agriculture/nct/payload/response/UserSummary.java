package com.agriculture.nct.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSummary {
    private int id;
    private String username;
    private String name;
    private String role;
}
