package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("select t.group from ScheduleTemplate t "
                   + "where t.professor.id = :professorId "
                   + "and t.course.id = :courseId")
    List<Group> findAllByProfessorAndCourse(
            @Param("professorId") long professorId,
            @Param("courseId") long courseId);

}