package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.ai.training.service.ImageService;
import org.springframework.ai.image.Image;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Rohit Muneshwar
 * @created on 2/27/2026
 *
 *
 */
@Tag(name = "Lab9: Image Generation", description = """
        Image generation examples
        """)
@RestController
@RequestMapping("/ai-image")
public class ImageController {
    private final ImageService imageService;
    public ImageController(ImageService imageService){
        this.imageService = imageService;
    }

    @Operation(summary = "Text to image generation", description = """
            Text to image generation
            """)
    @GetMapping("/v1/gen")
    public Image getAnswer(@RequestParam(name = "context", defaultValue = "summer days") String context,
                           @RequestParam(name = "image-width", defaultValue = "1792") int width,
                           @RequestParam(name = "image-height", defaultValue = "1024") int height){
        return imageService.generate(context, width, height);
    }
}
