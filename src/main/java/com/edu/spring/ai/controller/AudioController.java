package com.edu.spring.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.edu.spring.ai.service.AudioService;
import org.springframework.web.bind.annotation.*;


/**
 * @author Rohit Muneshwar
 * @created on 2/27/2026
 *
 *  Audio to text with SpringAI
 *         1. Conversion of text into audio
 *         2. Converting audio to text (speech to text/Transcription)
 *         3. Generating images
 *
 *         Transcription
 *           - to convert audio file to text in spring ai like whisper, gpt-4o-transcribe-gpt-4o-mini-transcribe
 *
 */
@Tag(name = "Lab8: Audio", description = """
        Audio to text with SpringAI
        """)
@RestController
@RequestMapping("/audio")
public class AudioController {
    private final AudioService audioService;
    public AudioController(AudioService audioService){
        this.audioService = audioService;
    }

    @Operation(summary = "Audio To Text", description = """
            Sample audio is present in the resources/audio folder. This examples uses manual OpneAiAudioApi config.
            """)
    @GetMapping("/v1/transcribe")
    public String getAnswerUsingManualSettings(@RequestParam(name = "filename", defaultValue = "java.m4a") String fileName){
        return audioService.getAnswerWithManualConfig(fileName);
    }

    @Operation(summary = "Audio To Text", description = """
            Sample audio is present in the resources/audio folder.
            """)
    @GetMapping("/v2/transcribe")
    public String getAnswer(@RequestParam(name = "filename", defaultValue = "java.m4a") String fileName){
        return audioService.getAnswer(fileName);
    }
}
