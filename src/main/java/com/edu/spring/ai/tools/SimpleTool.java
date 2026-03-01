package com.edu.spring.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Rohit Muneshwar
 * @created on 2/12/2026
 *
 *
 */
@Component
public class SimpleTool {
    // information tool: @Tool - Declarative approach for tool calling
    @Tool(description = "Get the current date and time in users zone")
    public String getCurrentDateTime(){
        IO.println("inside tool getCurrentDateTime");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    // action tool: set an alarm
    @Tool(description = "set the alarm for given time")
    public void setAlarm(@ToolParam(description = "Time in ISO-8601 format") String time){
        var dateTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        IO.println("Set the alarm for given time: "+dateTime);
    }

    // returnDirect: whether the tool should be returned directly to the client instead of
    // sending it back to LLM to compose the final response.
    // default is false: it goes to LLM
    @Tool(name = "fx_rate",
            description = "Get current FX rate from base to quote (USD to INR)",
            returnDirect = true)
    public String getRate(String base, String quote){
        return String.format("Rate %s-%s=%.4f", base, quote, 110.9599);
    }
}
