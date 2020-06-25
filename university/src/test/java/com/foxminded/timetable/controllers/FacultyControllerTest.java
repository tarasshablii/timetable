package com.foxminded.timetable.controllers;

import com.foxminded.timetable.exceptions.SessionExpiredException;
import com.foxminded.timetable.forms.ScheduleForm;
import com.foxminded.timetable.forms.ScheduleOption;
import com.foxminded.timetable.forms.utility.DaySchedule;
import com.foxminded.timetable.forms.utility.MonthSchedule;
import com.foxminded.timetable.forms.utility.TwoWeekSchedule;
import com.foxminded.timetable.forms.utility.WeekSchedule;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicate;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateProfessorId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
@Import(ControllersTestConfig.class)
class FacultyControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimetableFacade   timetableFacade;
    @MockBean
    private ScheduleFormatter scheduleFormatter;

    @Test
    public void getListShouldClearSessionAndReturnListView() throws Exception {

        mvc.perform(get("/timetable/faculty/list"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttributeDoesNotExist("student",
                        "professor"))
                .andExpect(view().name("faculty/list"));
    }

    @Test
    public void getListShouldRequestProfessorsFromServiceAndAddToModel()
            throws Exception {

        List<Professor> professors = Collections.emptyList();
        given(timetableFacade.getProfessors()).willReturn(professors);

        mvc.perform(get("/timetable/faculty/list"))
                .andExpect(model().attribute("professors", professors));

        then(timetableFacade).should().getProfessors();
    }

    @Test
    public void getListShouldAddFlashMassagesToModel() throws Exception {

        mvc.perform(get("/timetable/faculty/list"))
                .andExpect(model().attributeExists("errorAlert",
                        "sessionExpired"));
    }

    @Test
    public void postListShouldRequestProfessorFromServiceCheckPresenceAddToSessionAndRedirectToHome()
            throws Exception {

        long id = 1L;
        Professor professor = mock(Professor.class);
        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.of(professor));

        ResultActions resultActions = mvc.perform(
                post("/timetable/faculty/list").param("professorId",
                        String.valueOf(id)));
        resultActions.andExpect(
                request().sessionAttribute("professor", professor))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timetable/faculty/professor/home"));

        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void postListShouldRedirectToListIfProfessorNotPresent()
            throws Exception {

        long id = 1L;
        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.empty());

        ResultActions resultActions = mvc.perform(
                post("/timetable/faculty/list").param("professorId",
                        String.valueOf(id)));
        resultActions.andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/faculty/list"));

        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void getHomeShouldRequestTodaysScheduleFromFormatterAndReturnHomeView()
            throws Exception {

        boolean filtered = true;
        long id = 1L;
        SchedulePredicate predicate = new SchedulePredicateProfessorId(id);
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(id);
        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class),
                any(LocalDate.class), anyBoolean())).willReturn(daySchedule);

        mvc.perform(get("/timetable/faculty/professor/home").sessionAttr(
                "professor", professor))
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(view().name("faculty/professor/home"));

        then(scheduleFormatter).should()
                .prepareDaySchedule(predicate, LocalDate.now(), filtered);
    }

    @Test
    public void getTwoWeekShouldRequestScheduleFromFormatterAndReturnTwoWeekView()
            throws Exception {

        Professor professor = mock(Professor.class);
        TwoWeekSchedule twoWeekSchedule = mock(TwoWeekSchedule.class);
        given(scheduleFormatter.prepareTwoWeekSchedule()).willReturn(
                twoWeekSchedule);

        mvc.perform(get("/timetable/faculty/professor/two_week").sessionAttr(
                "professor", professor))
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute("twoWeekSchedule", twoWeekSchedule))
                .andExpect(view().name("faculty/professor/schedule/two_week"));

        then(scheduleFormatter).should().prepareTwoWeekSchedule();
    }

    @Test
    public void getScheduleShouldRedirectToHome() throws Exception {

        mvc.perform(get("/timetable/faculty/professor/schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timetable/faculty/professor/home"));
    }

    @Test
    public void postScheduleShouldRequestAndDisplayDaySchedulePerFormRequest()
            throws Exception {

        boolean filtered = true;
        long id = 1L;
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(id);
        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getScheduleOption()).willReturn(ScheduleOption.DAY);
        given(scheduleForm.getId()).willReturn(id);
        given(scheduleForm.isFiltered()).willReturn(filtered);
        SchedulePredicate predicate = new SchedulePredicateProfessorId(id);
        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class),
                any(LocalDate.class), anyBoolean())).willReturn(daySchedule);

        RequestBuilder requestBuilder =
                post("/timetable/faculty/professor/schedule").sessionAttr(
                        "professor", professor)
                        .flashAttr("scheduleForm", scheduleForm);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(view().name("faculty/professor/schedule/day"));

        then(scheduleFormatter).should()
                .prepareDaySchedule(predicate, date, filtered);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayWeekSchedulePerFormRequest()
            throws Exception {

        boolean filtered = true;
        long id = 1L;
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(id);
        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getDateDescription()).willReturn("");
        given(scheduleForm.getScheduleOption()).willReturn(ScheduleOption.WEEK);
        given(scheduleForm.getId()).willReturn(id);
        given(scheduleForm.isFiltered()).willReturn(filtered);
        SchedulePredicate predicate = new SchedulePredicateProfessorId(id);
        WeekSchedule weekSchedule = mock(WeekSchedule.class);
        given(scheduleFormatter.prepareWeekSchedule(
                any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(weekSchedule);

        RequestBuilder requestBuilder =
                post("/timetable/faculty/professor/schedule").sessionAttr(
                        "professor", professor)
                        .flashAttr("scheduleForm", scheduleForm);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attribute("weekSchedule", weekSchedule))
                .andExpect(view().name("faculty/professor/schedule/week"));

        then(scheduleFormatter).should()
                .prepareWeekSchedule(predicate, date, filtered);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayMonthSchedulePerFormRequest()
            throws Exception {

        boolean filtered = true;
        long id = 1L;
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(id);
        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getDateDescription()).willReturn("");
        given(scheduleForm.getScheduleOption()).willReturn(
                ScheduleOption.MONTH);
        given(scheduleForm.getId()).willReturn(id);
        given(scheduleForm.isFiltered()).willReturn(filtered);
        SchedulePredicate predicate = new SchedulePredicateProfessorId(id);
        MonthSchedule monthSchedule = mock(MonthSchedule.class);
        given(scheduleFormatter.prepareMonthSchedule(
                any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(monthSchedule);

        RequestBuilder requestBuilder =
                post("/timetable/faculty/professor/schedule").sessionAttr(
                        "professor", professor)
                        .flashAttr("scheduleForm", scheduleForm);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attribute("monthSchedule", monthSchedule))
                .andExpect(view().name("faculty/professor/schedule/month"));

        then(scheduleFormatter).should()
                .prepareMonthSchedule(predicate, date, filtered);
    }

    @Test
    public void getCoursesShouldRequestAndDisplayProfessorCoursesWithAttendees()
            throws Exception {

        Professor professor = mock(Professor.class);
        Course course = mock(Course.class);
        given(professor.getCourses()).willReturn(
                Collections.singleton(course));
        Student attendee = mock(Student.class);
        Group group = mock(Group.class);
        given(attendee.getGroup()).willReturn(group);
        given(group.getName()).willReturn("");
        List<Student> courseAttendees = Collections.singletonList(attendee);
        given(timetableFacade.getCourseAttendees(any(Course.class),
                any(Professor.class))).willReturn(courseAttendees);
        Map<Course, List<Student>> allCourseAttendees = new HashMap<>();
        allCourseAttendees.put(course, courseAttendees);

        mvc.perform(get("/timetable/faculty/professor/courses").sessionAttr(
                "professor", professor))
                .andExpect(status().isOk())
                .andExpect(model().attribute("allCourseAttendees",
                        allCourseAttendees))
                .andExpect(view().name("faculty/professor/courses"));

        then(timetableFacade).should().getCourseAttendees(course, professor);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/timetable/faculty/professor/home",
            "/timetable/faculty/professor/two_week",
            "/timetable/faculty/professor/courses" })
    public void getProfessorPagesShouldThrowExceptionIfNoProfessorInSession(
            String uri) throws Exception {


        MvcResult mvcResult = mvc.perform(get(uri))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sessionExpired"))
                .andExpect(redirectedUrl("/timetable/faculty/list"))
                .andReturn();

        Optional<SessionExpiredException> sessionExpiredException =
                Optional.ofNullable(
                        (SessionExpiredException) mvcResult.getResolvedException());

        assertThat(sessionExpiredException).isPresent()
                .containsInstanceOf(SessionExpiredException.class);
    }

    @Test
    public void postScheduleShouldThrowExceptionIfNoProfessorInSession()
            throws Exception {

        MvcResult mvcResult =
                mvc.perform(post("/timetable/faculty/professor/schedule"))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(flash().attributeExists("sessionExpired"))
                        .andExpect(redirectedUrl("/timetable/faculty/list"))
                        .andReturn();

        Optional<SessionExpiredException> sessionExpiredException =
                Optional.ofNullable(
                        (SessionExpiredException) mvcResult.getResolvedException());

        assertThat(sessionExpiredException).isPresent()
                .containsInstanceOf(SessionExpiredException.class);
    }

}