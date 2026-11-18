package com.hostel.smarthostel.model;

import jakarta.persistence.*;

/**
 * ABSTRACTION + INHERITANCE: common base of all hostel users.
 * ENCAPSULATION: fields are private; the password has no getter.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type")
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    private String phone;

    @Column(nullable = true)
    private String email;

    protected User() { }

    protected User(String username, String password, String fullName, String phone) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password is required");
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("Full name is required");
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
    }

    /** POLYMORPHISM: every subclass answers these differently. */
    public abstract Role getRole();
    public abstract String getDashboardTitle();

    public boolean checkPassword(String input) {
        return password.equals(input);
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
}
