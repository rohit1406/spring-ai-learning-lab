package com.edu.spring.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;

/**
 * @author Rohit Muneshwar
 * @created on 2/27/2026
 *
 *
 */
@Service
@Slf4j
public class ImageService {
    private final OpenAiImageModel openAiImageModel;
    public ImageService(OpenAiImageModel openAiImageModel){
        this.openAiImageModel = openAiImageModel;
    }

    /**
     * generate image from text
     * @param context
     * @return
     */
    public Image generate(String context, int width, int height) {
        OpenAiImageOptions imageOptions = OpenAiImageOptions.builder()
                .N(1) // Number of images to be generated, depends on the model being used
                .height(height)
                .width(width)
                .build();
        ImagePrompt imagePrompt = new ImagePrompt(context, imageOptions);
        return openAiImageModel.call(imagePrompt)
                .getResult().getOutput();
    }
}
