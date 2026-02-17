package me.ai.training.dto;

import java.util.List;

/**
 * @author Rohit Muneshwar
 */
public class Request{
    String model="gpt-4.1";
    List<Message> messages;
    int maxTokens = 100;

    @Override
    public String toString() {
        return "{" +
                "\"model\":" + model + ',' +
                ", \"messages\":" + messages +
                ", \"maxTokens\":" + maxTokens +
                ", \"temperature\":" + temperature +
                '}';
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    double temperature = 0.7;
}
