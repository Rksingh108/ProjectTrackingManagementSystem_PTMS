package com.ptms.app.model;

import java.time.LocalDate;

/**
 * Represents a row in the `users` table.
 * Covers Admin, Project Manager, Team Lead, and Team Member —
 * distinguished by the roleName field.
 */
public class User {

    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;   // hashed, never stored/handled in plain text
    private String roleName;   // e.g. ADMIN, PROJECT_MANAGER, TEAM_LEAD, TEAM_MEMBER
    private LocalDate dateOfBirth;
    private String mobileNumber;
    private String gender;

    public User() {
    }

    public User(int id, String firstName, String lastName, String username, String email,
                String password, String roleName, LocalDate dateOfBirth, String mobileNumber, String gender) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.password = password;
        this.roleName = roleName;
        this.dateOfBirth = dateOfBirth;
        this.mobileNumber = mobileNumber;
        this.gender = gender;
    }

    // Constructor for creating a new user before an id is assigned by the DB
    public User(String firstName, String lastName, String username, String email,
                String password, String roleName, LocalDate dateOfBirth, String mobileNumber, String gender) {
        this(0, firstName, lastName, username, email, password, roleName, dateOfBirth, mobileNumber, gender);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roleName='" + roleName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}