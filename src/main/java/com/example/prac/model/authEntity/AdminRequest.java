package com.example.prac.model.authEntity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "admin_request")
@Data
public class AdminRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", referencedColumnName = "id")
    private User requester;

    @Column(name = "approved_by_all", nullable = false)
    private boolean approvedByAll;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "admin_request_approved_by",
            joinColumns = @JoinColumn(name = "admin_request_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> approvedBy;
}
