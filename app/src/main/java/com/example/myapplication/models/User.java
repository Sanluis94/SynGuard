package com.example.myapplication.models;

public class User {
    private String fullName;
    private String email;
    private String userType;
    private String caregiverId;
    private String phone;

    public User(String fullName, String email, String userType, String caregiverId, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
        this.caregiverId = caregiverId;
        this.phone = phone;
    }

    // Getters e setters

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
        return userType;
    }

    public void setRole(String role) {
        this.userType = role;
    }

    public String getCaregiverId() {
        return caregiverId;
    }

    public void setCaregiverId(String caregiverId) {
        this.caregiverId = caregiverId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Método auxiliar para gerar o ID único do cuidador
    public static String generateCaregiverId() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder id = new StringBuilder();

        // Gerar 4 letras aleatórias
        for (int i = 0; i < 4; i++) {
            int index = (int) (Math.random() * letters.length());
            id.append(letters.charAt(index));
        }

        // Gerar 2 números aleatórios
        for (int i = 0; i < 2; i++) {
            int digit = (int) (Math.random() * 10);
            id.append(digit);
        }

        return id.toString();
    }
}
