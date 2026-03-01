package com.edu.spring.ai.controller;

import com.edu.spring.ai.service.SnapSubtitleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Rohit Muneshwar
 * @created on 3/1/2026
 *
 *
 */
@Tag(name = "Practice: Assignments")
@RestController
@RequestMapping("/subtitle")
public class SnapSubtitleController {
    private final SnapSubtitleService snapSubtitleService;
    public SnapSubtitleController(SnapSubtitleService snapSubtitleService){
        this.snapSubtitleService = snapSubtitleService;
    }
    @Operation(summary = "Snap to the position in the subtitle file as per asked context",
    description = """
            It returns the position code of the provided context from the file.
            The subtitle file classpath://subtitles/TheLastSignal-scifi-drama-fictional-short-film.srt is already saved in the vector db.
            If not then save it using /save-last-signal-subtitle-data endpoint first.
            
            It is the fictional sci-fi drama story. Check the file and provide a short context where you want to snap to the file.
            Then this endpoint will return back the code of that location from the file.
            """)
    @GetMapping("/snap-to-position")
    public String snapToPosition(@RequestParam String context){
        return snapSubtitleService.snapToPosition(context);
    }
}
