package com.foxminded.timetable.service.printer.assembler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ColumnWriterTest {

    private ColumnWriter columnWriter;

    @ParameterizedTest
    @CsvSource(value = { "test:longerTest", "longerTest:test", },
               delimiter = ':')
    public void shouldCalculateMaxColumnWidthAtConstruction(String first,
            String second) {

        String title = first;
        List<String> items = Arrays.asList(second);
        int expectedWidth = 10;

        columnWriter = new ColumnWriter(title, items);
        int actualWidth = columnWriter.getWidth();

        assertThat(actualWidth).isEqualTo(expectedWidth);
    }

    @Test
    public void shouldSetWidthToOneGivenEmptyStringsAtInput() {

        String empty = "";
        List<String> items = Arrays.asList(empty);
        int expectedWidth = 1;

        columnWriter = new ColumnWriter(empty, items);
        int actualWidth = columnWriter.getWidth();

        assertThat(actualWidth).isEqualTo(expectedWidth);
    }

}
