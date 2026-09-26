package com.ptms.app.model;

/**
 * Maps directly to the `clients` table.
 */
public class Client {

    private Integer id;              // null until saved (auto-increment in DB)
    private String name;
    private String email;
    private String phone;
    private String companyName;

    public Client() {
    }

    public Client(String name, String email, String phone, String companyName) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.companyName = companyName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return "Client{id=" + id +
                ", name='" + name + '\'' +
                ", companyName='" + companyName + '\'' +
                '}';
    }
}