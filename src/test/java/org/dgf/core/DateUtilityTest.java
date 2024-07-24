package org.dgf.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateUtilityTest {

    @Test
    public void ParseDatetime_ReturnSuccessful()
    {
        String expect = "2024-07-19T12:50:10.319287Z";
        LocalDateTime time = DateUtility.parse(expect);
        String actual = DateUtility.format(time);
        assertEquals(expect, actual);

        List<Double> li = new ArrayList<>();
        li.add(123.3334);
        li.add(2.333);
        assertTrue(li.contains(123.3334));
    }
}
