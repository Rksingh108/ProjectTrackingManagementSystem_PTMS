package com.ptms.app.model;

import java.time.LocalDate;

/**
 * Maps directly to the `users` table.
 * One class, one `role` field — covers Admin, Project Manager, Team Lead,
 * and Team Member without needing separate subclasses.
 */
public class User {

    public enum Role {
        ADMIN, PROJECT_MANAGER, TEAM_LEAD, TEAM_MEMBER
    }

    private Integer id;              // null until saved (auto-increment in DB)
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;         // holds a hash, never plaintext
    private Role role;
    private LocalDate dateOfBirth;
    private String mobileNumber;
    private String gender;

    public User() {
    }

    public User(String firstName, String lastName, String username, String email,
                String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // --- getters and setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "User{id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}






