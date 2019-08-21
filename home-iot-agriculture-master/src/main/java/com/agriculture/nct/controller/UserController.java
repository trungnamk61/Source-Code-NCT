package com.agriculture.nct.controller;

import com.agriculture.nct.database.DBWeb;
import com.agriculture.nct.model.User;
import com.agriculture.nct.exception.ResourceNotFoundException;
import com.agriculture.nct.payload.response.UserIdentityAvailability;
import com.agriculture.nct.payload.response.UserProfile;
import com.agriculture.nct.payload.response.UserSummary;
import com.agriculture.nct.security.CurrentUser;
import com.agriculture.nct.security.UserPrincipal;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Log4j2
public class UserController {

    private final DBWeb dbWeb;

    @Autowired
    public UserController(DBWeb dbWeb) {
        this.dbWeb = dbWeb;
    }

    @GetMapping("/user/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        return new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName(), currentUser.getAuthorities().iterator().next().getAuthority());
    }

    @GetMapping("/user/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        Boolean isAvailable = !dbWeb.getUserByUsername(username).isPresent();
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/user/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
        Boolean isAvailable = !dbWeb.getUserByEmail(email).isPresent();
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/users/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
        User user = dbWeb.getUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return new UserProfile(user.getId(), user.getUsername(), user.getFullName(), user.getCreateTime());
    }
}
