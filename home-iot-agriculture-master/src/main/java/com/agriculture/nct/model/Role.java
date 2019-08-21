package com.agriculture.nct.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    ROLE_UNKNOWN(0,"unknown","Unknown"), ROLE_ADMIN(1,"admin","Administrators"), ROLE_MOD(2,"techmod","Technical moderators"), ROLE_USER(3,"customer","Customers");

    private int id;

    private String name;

    private String fullName;

    public static Role getById(int id) {
        for(Role e : values()) {
            if(e.id == id) return e;
        }
        return ROLE_UNKNOWN;
    }
}