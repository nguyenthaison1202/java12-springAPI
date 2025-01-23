package com.group1.MockProject.service;

import com.group1.MockProject.dto.response.*;

public interface SavedCourseService {
  GetSavedCourseResponse getSavedCoursesByEmail(String email);
}
