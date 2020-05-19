package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.StudentDao;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private final Group          group   = new Group(1L, "group");
    private final Student        student = new Student(1L, "one", "one", group);
    @Mock
    private       StudentDao     repository;
    @InjectMocks
    private       StudentService service;

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void saveShouldDoNothingAndReturnStudentIfNoGroup() {

        Student expected = new Student("new", "new");

        Student actual = service.save(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldAddStudentToRepositoryIfNew() {

        Student expected = new Student("new", "new");
        expected.setGroup(group);
        given(repository.save(any(Student.class))).willReturn(expected);

        Student actual = service.save(expected);

        then(repository).should().save(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldUpdateStudentGroupInRepositoryIfExisting() {

        given(repository.update(any(Student.class))).willReturn(student);

        Student actual = service.save(student);

        then(repository).should().update(student);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(student);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Student> students = Collections.singletonList(student);
        given(repository.saveAll(anyList())).willReturn(students);

        List<Student> actual = service.saveAll(students);

        then(repository).should().saveAll(students);
        assertThat(actual).hasSameElementsAs(students);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<Student> expected = Collections.emptyList();

        List<Student> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<Student> students = Collections.singletonList(student);
        given(repository.findAll()).willReturn(students);

        List<Student> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(students);
    }

    @Test
    public void findAllInGroupsShouldDelegateToRepository() {

        List<Group> groups = Collections.singletonList(group);
        List<Student> students = Collections.singletonList(student);
        given(repository.findAllInGroups(anyList())).willReturn(students);

        List<Student> actual = service.findAllInGroups(groups);

        then(repository).should().findAllInGroups(groups);
        assertThat(actual).isEqualTo(students);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        given(repository.findById(anyLong())).willReturn(Optional.of(student));

        Optional<Student> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isPresent().contains(student);
    }

}
