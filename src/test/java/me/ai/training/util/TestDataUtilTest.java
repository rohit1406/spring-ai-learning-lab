package me.ai.training.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Rohit Muneshwar
 * @created on 2/23/2026
 *
 *
 */
public class TestDataUtilTest {
    @Test
    void testSplit(){
        List<String> list = DataUtil.getBigBuckBunnySubtitle();
        Assertions.assertNotNull(list);
    }
}
