package com.foxminded.timetable.dao.jdbc;

import com.foxminded.timetable.dao.StudentDao;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ComponentScan
@Sql("classpath:schema.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class JdbcStudentDaoTest {

    private final Group                      groupOne     = new Group(1L,
            "one");
    private final Group                      groupTwo     = new Group(2L,
            "two");
    private final List<Group>                groups       = Arrays.asList(
            groupOne, groupTwo);
    private final Student                    studentOne   = new Student(1L,
            "one", "one", groupOne);
    private final Student                    studentTwo   = new Student(2L,
            "two", "two", groupOne);
    private final Student                    studentThree = new Student(3L,
            "three", "three", groupTwo);
    private final List<Student>              students     = Arrays.asList(
            studentOne, studentTwo, studentThree);
    private final String                     selectSql    =
            "SELECT students.id, students.first_name, "
                    + "students.last_name, groups.id, groups.name FROM "
                    + "students "
                    + "LEFT JOIN groups ON students.group_id = groups.id";
    @Autowired
    private       NamedParameterJdbcTemplate jdbc;
    private       StudentDao                 studentRepository;

    @BeforeEach
    private void setUp() {

        this.studentRepository = new JdbcStudentDao(jdbc);
    }

    @Test
    @Sql("classpath:preload_sample_data_student_test.sql")
    public void countShouldReturnCorrectAmountOfStudents() {

        long expected = 3L;
        long actual = studentRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_student_test.sql")
    public void findAllShouldRetrieveCorrectListOfStudents() {

        List<Student> actual = studentRepository.findAll();

        assertThat(actual).hasSameElementsAs(students);
    }

    @Test
    @Sql("classpath:preload_sample_data_student_test.sql")
    public void findAllByGroupsShouldRetrieveCorrectListOfStudents() {

        List<Group> requestedGroups = Arrays.asList(groupOne);
        List<Student> actual = studentRepository.findAllInGroups(
                requestedGroups);

        assertThat(actual).containsOnly(studentOne, studentTwo)
                .doesNotContain(studentThree);
    }

    @Test
    @Sql("classpath:preload_sample_data_student_test.sql")
    public void findByIdShouldReturnCorrectStudent() {

        Student expectedStudent = studentThree;
        Optional<Student> actualStudent = studentRepository.findById(
                expectedStudent.getId());

        assertThat(actualStudent).isNotEmpty().contains(expectedStudent);
    }

    @Test
    @Sql("classpath:preload_sample_data_student_test.sql")
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        Optional<Student> actualStudent = studentRepository.findById(999L);

        assertThat(actualStudent).isEmpty();
    }

    @Test
    public void saveShouldAddStudent() {

        String sqlGroup = "INSERT INTO groups (name) VALUES (:name)";
        jdbc.batchUpdate(sqlGroup, SqlParameterSourceUtils.createBatch(groups));

        Student expected = studentRepository.save(studentOne);

        String filter = " WHERE students.id = :id";
        Student actual = jdbc.queryForObject(selectSql + filter,
                new MapSqlParameterSource("id", studentOne.getId()),
                (ResultSet rs, int rowNum) -> new Student(rs.getLong(1),
                        rs.getString(2), rs.getString(3),
                        new Group(rs.getLong(4), rs.getString(5))));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_student_test.sql")
    public void udpateShouldUpdateStudentGroup() {

        Student expected = new Student(studentOne.getId(),
                studentOne.getFirstName(), studentOne.getLastName(), groupTwo);

        studentRepository.update(expected);

        String filter = " WHERE students.id = :id";
        MapSqlParameterSource paramSource = new MapSqlParameterSource("id",
                expected.getId());
        RowMapper<Student> studentRowMapper =
                (ResultSet rs, int rowNum) -> new Student(
                rs.getLong(1), rs.getString(2), rs.getString(3),
                new Group(rs.getLong(4), rs.getString(5)));
        Student actual = jdbc.queryForObject(selectSql + filter, paramSource,
                studentRowMapper);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAllShouldSaveAllStudents() {

        String sqlGroup = "INSERT INTO groups (name) VALUES (:name)";
        jdbc.batchUpdate(sqlGroup, SqlParameterSourceUtils.createBatch(groups));

        studentRepository.saveAll(students);

        RowMapper<Student> studentRowMapper =
                (ResultSet rs, int rowNum) -> new Student(
                rs.getLong(1), rs.getString(2), rs.getString(3),
                new Group(rs.getLong(4), rs.getString(5)));
        List<Student> actual = jdbc.query(selectSql, studentRowMapper);

        assertThat(actual).hasSameElementsAs(students);
    }

}
