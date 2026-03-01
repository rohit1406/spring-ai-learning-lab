package com.edu.spring.ai.util;

/**
 * @author Rohit Muneshwar
 * @created on 2/11/2026
 *
 *
 */
public interface PromptMessages {
    String EXPLAIN_KAFKA_ZERO_SHOT_PROMPTING = "Explain kafka in simple terms";
    String ASK_QUESTION_WITH_FEW_SHOT_PROMPTING = """
            You are an AI that generated Java interview questions.
           Example:
           Topic: OOP
           Question: What is encapsulation in Java?

           Example:
           Topic: Collections
           Question: What is ArrayList in Java?

           Now generate a question for:
           Topic: Multithreading
            
            """;
    String ANALYSE_CAUSE_WITH_CHAIN_OF_THOUGHT_PROMPTING = """
            An application is slow during peak hours.
            Think step by step and identify possible causes.
            """;
}
