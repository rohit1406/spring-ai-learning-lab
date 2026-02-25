package me.ai.training.exceptions;

import lombok.Data;

/**
 * @author Rohit Muneshwar
 * @created on 2/24/2026
 *
 *
 */
@Data
public class DataStoreException extends RuntimeException{
    private final String message;
}
