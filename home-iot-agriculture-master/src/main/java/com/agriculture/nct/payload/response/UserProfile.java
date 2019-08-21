package com.agriculture.nct.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class UserProfile {
    private int id;
    private String username;
    private String name;
    private Date joinedAt;
}
