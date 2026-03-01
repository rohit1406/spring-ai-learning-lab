package com.edu.spring.ai.dto;

import java.util.List;

/**
 * @author Rohit Muneshwar
 */
public class Response {
    private List<Messages> choices;

    public List<Messages> getChoices() {
        return choices;
    }

    public void setChoices(List<Messages> choices) {
        this.choices = choices;
    }
}
