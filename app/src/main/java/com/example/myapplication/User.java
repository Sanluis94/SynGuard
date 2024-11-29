package com.example.myapplication;

public class User {

    private String userId;
    private String fullName;
    private String email;
    private String role;
    private String phone;
    private String caregiverId;

    public User() {
    }

    public User(String userId, String fullName, String email, String role, String phone, String caregiverId) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.phone = phone;
        this.caregiverId = caregiverId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(String caregiverId) {
        this.caregiverId = caregiverId;
    }
}
