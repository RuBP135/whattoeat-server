package com.rubp.whattoeat.server.account;

import jakarta.persistence.*;

@Entity
@Table(name = "account")
public class AccountEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 10)
    private String uid;


}
