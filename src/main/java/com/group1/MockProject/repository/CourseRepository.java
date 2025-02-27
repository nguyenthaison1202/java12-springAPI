package com.group1.MockProject.repository;

import com.group1.MockProject.entity.Category;
import com.group1.MockProject.entity.Course;
import com.group1.MockProject.entity.Instructor;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    Optional<Course> findById(int id);
    List<Course> findCourseByCategory(Category category);
    List<Course> findByInstructorId(int instructorId);
    List<Course> findByInstructor(Instructor instructor);
    Course findCourseByTitle(String title);

    long countByStatus(@NotNull int status);

    @Query("SELECT COALESCE(SUM(c.price), 0) FROM Course c")
    double calculateTotalRevenue();
    
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Course c JOIN c.reviews r")
    double calculateAverageRating();
    
    @Query("SELECT COUNT(r) FROM Course c JOIN c.reviews r")
    long countTotalReviews();
}
