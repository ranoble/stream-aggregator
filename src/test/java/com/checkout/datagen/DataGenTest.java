package com.checkout.datagen;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataGenTest {

    public static List<String> OPTIONS = Arrays.asList(
            "ZERO", "ONE", "TWO"
    );

    @Test
    @SneakyThrows
    public void testOptionUdf() {
        DataGen.GenerateOption udf = new DataGen.GenerateOption();
        assertEquals(udf.eval(0,OPTIONS), "ZERO");
        assertEquals(udf.eval(1,OPTIONS), "ONE");
        assertEquals(udf.eval(2,OPTIONS), "TWO");
        assertEquals(udf.eval(3,OPTIONS), "ZERO");
    }

}
