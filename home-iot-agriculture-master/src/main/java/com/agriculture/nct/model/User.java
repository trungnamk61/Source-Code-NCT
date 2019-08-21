package com.agriculture.nct.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class User {
    private int id;
    private String username;
    private String fullName;
    private String email;
    @JsonIgnore
    private String password;
    private int roleId;
    private Date createTime;
    private Date lastLogin;

    public User(String username, String fullName, String email, String password, int roleId) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
    }
}
