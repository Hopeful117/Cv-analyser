package com.hopeful117.cv_analyzer.TestExtractor;

import com.hopeful117.cv_analyzer.extractor.JobImportanceDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TestImportanceDetector {

    private final JobImportanceDetector importanceDetector = new JobImportanceDetector();

    @Test
    public void testDetectMandatory() {
        String jobOfferText = "We are looking for a candidate with mandatory Java skills.";
        String keyword = "Java";


        var result = importanceDetector.detect(keyword, jobOfferText);

        assertEquals( 30, result.getWeight());
        assertTrue(result.isMandatory());
    }

    @Test
    public void testDetectOptional() {
        String jobOfferText = "We prefer candidates with optional Python skills.";
        String keyword = "Python";
        var result = importanceDetector.detect(keyword, jobOfferText);
        assertEquals( 10, result.getWeight());
        assertFalse(result.isMandatory());
    }

    @Test
    public void testDetectNeutral() {
        String jobOfferText = "Experience with C++ is a plus.";
        String keyword = "C++";
        var result = importanceDetector.detect(keyword, jobOfferText);
        assertEquals( 20, result.getWeight());
        assertFalse(result.isMandatory());
    }

    @Test
    public void testDetectCaseInsensitive() {
        String jobOfferText = "We are looking for a candidate with MANDATORY Java skills.";
        String keyword = "Java";
        var result = importanceDetector.detect(keyword, jobOfferText);
        assertEquals( 30, result.getWeight());
        assertTrue(result.isMandatory());
    }

    @Test
    public void testDetectNoKeyword() {
        String jobOfferText = "We are looking for a candidate with mandatory Java skills.";
        String keyword = "Python";
        var result = importanceDetector.detect(keyword, jobOfferText);
        assertEquals( 20, result.getWeight());
        assertFalse(result.isMandatory());
    }
}
