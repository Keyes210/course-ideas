package com.alexlowe.courses.model;

import java.util.List;

/**
 * Created by Keyes on 6/19/2016.
 */
public interface CourseIdeaDAO {
    boolean add(CourseIdea idea);

    List<CourseIdea> findAll();
}
