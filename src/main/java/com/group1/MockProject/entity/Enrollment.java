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
public class Enrollment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private LocalDateTime enrollmentDate = LocalDateTime.now();

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;
}
