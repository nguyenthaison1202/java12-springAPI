package com.group1.MockProject.service.implementation;

import static org.junit.jupiter.api.Assertions.*;

import com.group1.MockProject.dto.AnalyticDTO;
import com.group1.MockProject.entity.*;
import com.group1.MockProject.repository.AnalyticRepository;
import com.group1.MockProject.repository.InstructorRepository;
import com.group1.MockProject.repository.SubscriptionRepository;
import com.group1.MockProject.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class AnalyticImplTest {
  @Mock private UserRepository userRepository;

  @Mock private InstructorRepository instructorRepository;

  @Mock private AnalyticRepository analyticRepository;

  @Mock private SubscriptionRepository subscriptionRepository;

  @InjectMocks private AnalyticImpl analyticImpl;

  private User mockUser;
  private Instructor mockInstructor;
  private Course mockCourse;
  private Analytic mockAnalytic;
  private final String mockEmail = "mock@email.com";

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockInstructor = new Instructor();
    mockCourse = new Course();
    mockAnalytic = new Analytic();

    SavedCourse mockSavedCourse = new SavedCourse();
    MyCourse mockMyCourse = new MyCourse();

    mockUser.setInstructor(mockInstructor);
    mockInstructor.setUser(mockUser);
    mockInstructor.setCourses(List.of(mockCourse));
    mockCourse.setInstructor(mockInstructor);
    mockCourse.setPrice(0.0);
    mockCourse.setSavedCourses(List.of(mockSavedCourse));
    mockCourse.setMyCourses(List.of(mockMyCourse));
    mockAnalytic.setInstructor(mockInstructor);
    mockAnalytic.setSales(0.0);
    mockAnalytic.setVisitorCount(0);
    mockAnalytic.setSubscriptionCount(0);
  }

  @Test
  public void testGetInstructorAnalytic_Success() {
    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(instructorRepository.findInstructorByUser(Mockito.any(User.class)))
        .thenReturn(Optional.of(mockInstructor));
    Mockito.when(analyticRepository.getByInstructor(Mockito.any(Instructor.class)))
        .thenReturn(Optional.of(mockAnalytic));

    AnalyticDTO.AnalyticResponse response = analyticImpl.getInstructorAnalytic(mockEmail);
    Assertions.assertNotNull(response);
    Assertions.assertEquals(response.analytic, mockAnalytic);
  }

  @Test
  public void testGetInstructorAnalytic_DoNotHaveAccess() {
    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(instructorRepository.findInstructorByUser(Mockito.any(User.class)))
        .thenReturn(Optional.empty());

    Exception exception =
        Assertions.assertThrows(
            AccessDeniedException.class, () -> analyticImpl.getInstructorAnalytic(mockEmail));
    Assertions.assertEquals("Bạn không có quyền thực hiện thao tác này", exception.getMessage());
  }

  @Test
  public void testGetInstructorAnalytic_InstructorDoesNotHaveAnyCourse() {
    mockInstructor.setCourses(List.of());

    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(instructorRepository.findInstructorByUser(Mockito.any(User.class)))
        .thenReturn(Optional.of(mockInstructor));

    Exception exception =
        Assertions.assertThrows(
            EmptyResultDataAccessException.class,
            () -> analyticImpl.getInstructorAnalytic(mockEmail));
    Assertions.assertEquals("Không tìm thấy khóa học", exception.getMessage());
  }

  @Test
  public void testGetInstructorAnalytic_DoNotHaveAnyAnalytic() {
    Mockito.when(userRepository.findByEmail(Mockito.eq(mockEmail)))
        .thenReturn(Optional.of(mockUser));
    Mockito.when(instructorRepository.findInstructorByUser(Mockito.any(User.class)))
        .thenReturn(Optional.of(mockInstructor));
    Mockito.when(analyticRepository.getByInstructor(Mockito.any(Instructor.class)))
        .thenReturn(Optional.empty());
    Mockito.when(analyticRepository.save(Mockito.any(Analytic.class))).thenReturn(mockAnalytic);

    AnalyticDTO.AnalyticResponse response = analyticImpl.getInstructorAnalytic(mockEmail);
    Assertions.assertNotNull(response);
  }
}
