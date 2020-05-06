package com.foxminded.timetable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;

@ExtendWith(MockitoExtension.class)
class ReschedulingOptionServiceTest {

    @Mock
    private ReschedulingOptionDao repository;

    @InjectMocks
    private ReschedulingOptionService service;

    private ReschedulingOption option = new ReschedulingOption(1L,
            DayOfWeek.MONDAY, Period.FIRST, new Auditorium("A-01"));

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<ReschedulingOption> options = Arrays.asList(option);
        given(repository.saveAll(anyList())).willReturn(options);

        List<ReschedulingOption> actual = service.saveAll(options);

        then(repository).should().saveAll(options);
        assertThat(actual).hasSameElementsAs(options);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<ReschedulingOption> expected = Collections.emptyList();

        List<ReschedulingOption> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<ReschedulingOption> options = Arrays.asList(option);
        given(repository.findAll()).willReturn(options);

        List<ReschedulingOption> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(options);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        given(repository.findById(anyLong())).willReturn(Optional.of(option));

        Optional<ReschedulingOption> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isPresent().contains(option);
    }

    @Test
    public void findAllDayOptionsForShouldMapRepositoryOptionsToDate() {

        boolean weekParity = false;
        LocalDate date = LocalDate.MAX;
        Schedule schedule = mock(Schedule.class);
        List<ReschedulingOption> options = Arrays.asList(option);
        given(repository.findDayReschedulingOptionsForSchedule(anyBoolean(),
                any(LocalDate.class), any(Schedule.class))).willReturn(options);

        Map<LocalDate, List<ReschedulingOption>> actual = service
                .findAllDayOptionsFor(weekParity, date, schedule);

        then(repository).should().findDayReschedulingOptionsForSchedule(
                weekParity, date, schedule);
        assertThat(actual).containsOnlyKeys(date).containsValue(options);
    }

}