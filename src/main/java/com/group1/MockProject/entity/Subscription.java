package com.group1.MockProject.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscriptions")
public class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "created_at")
  private LocalDateTime createdAt = LocalDateTime.now();

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "student_id")
  private Student student;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "instructor_id")
  private Instructor instructor;

  @Column(name = "subscribed_at")
  private LocalDateTime subscribedAt = LocalDateTime.now();

  public LocalDateTime getSubscribedAt() {
    return subscribedAt;
  }

  public void setSubscribedAt(LocalDateTime subscribedAt) {
    this.subscribedAt = subscribedAt;
  }
}
