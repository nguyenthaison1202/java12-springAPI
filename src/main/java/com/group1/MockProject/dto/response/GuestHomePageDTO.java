package com.group1.MockProject.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GuestHomePageDTO {
    private List<CourseDTO> courseDTOs;
    private List<CategoryDTO> categoryDTOs;
}
