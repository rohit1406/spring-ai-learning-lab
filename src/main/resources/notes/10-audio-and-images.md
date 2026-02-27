# Working with Audio and Images
## Audio

### Audio to text with SpringAI
1. Conversion of text into audio
2. Converting audio to text (speech to text/Transcription)
3. Generating images

Transcription
- to convert audio file to text in spring ai like whisper, gpt-4o-transcribe-gpt-4o-mini-transcribe

#### Steps
1. Create springboot project with web, azure ai, lombok dependency

2. Configure api keys in application.properties
````
spring.ai.azure.openai.api-key=
spring.ai.azure.openai.endpoint=
spring.ai.azure.openai.chat.options.deployment-name=gpt-4.1
spring.ai.azure.openai.audio.transcription.options.deployment-name=whisper
````
3. We keep audio files inside resources folder(.mp3, .wav, .m4a, .mp4, .mpeg, .mpga,.webm)

4. Create class - OpenAIClient bean
````
@Configuration
public class AzureOpenAIClientConfig {

              @Bean
              public OpenAIClient openAIClient(
                             @Value("${spring.ai.azure.openai.endpoint}")
                             String endpoint,
                            
                             @Value("${spring.ai.azure.openai.api-key}")
                             String apiKey) {
                            
                             return new OpenAIClientBuilder()
                                                             .endpoint(endpoint)
                                                             .credential(new AzureKeyCredential(apiKey))
                                                             .buildClient();
              }
}
````
5. Create controller prg
````
@RestController
@RequestMapping("/audio")
public class AudioController {

              private final OpenAIClient openAIClient;

@Value("${spring.ai.azure.openai.audio.transcription.options.deployment-name}")
String deploymentname;

              public AudioController(OpenAIClient openAIClient) {
                             this.openAIClient = openAIClient;
              }
 
              @GetMapping("/transcribe")
              public String transcribeAudio(@RequestParam("filename")String name) {
                             //load audio file from classpath
                             ClassPathResource audioResource=new ClassPathResource(name);
                            
                             //convert audio to raw byte[]
                             try(InputStream in=audioResource.getInputStream()) {
                                           byte[] bytes=in.readAllBytes();
                                          
                                           AudioTranscriptionOptions opts=new AudioTranscriptionOptions(bytes)
                                                                        .setFilename(name)
                                                                     .setResponseFormat(AudioTranscriptionFormat.VERBOSE_JSON);
                                          
                                           return openAIClient.getAudioTranscription(deploymentname, audioResource.getFilename(), opts).getText();
                                          
                             }catch(Exception e) {
                                           throw new RuntimeException(e);
                             }
              }
}
````
6. Start the appl, run  http://localhost:1000/audio/transcribe?filename=sample2.m4a

## Image
### Text to image generation
````
@RestController
@RequestMapping("/v1/ai/image")
public class ImageGenerationController {

    private final AzureOpenAiImageModel imageClient;
 
    @Autowired
    public ImageGenerationController(AzureOpenAiImageModel imageClient) {
        this.imageClient = imageClient;
    }
 
    @GetMapping("/gen")
    public Image getImage() {
        AzureOpenAiImageOptions imageOptions = AzureOpenAiImageOptions.builder()
                .N(1) // Number of images to be generated, depends on the model being used
                .height(1024)
                .width(1792)
                .build();
 
        ImagePrompt imagePrompt = new ImagePrompt(PromptsConstants.BLR_OLD, imageOptions);
 
        return imageClient.call(imagePrompt).getResult().getOutput();
    }
}
````

---

## 🌟 Developer/Contributor
Name: Rohit Shamrao Muneshwar  
Email: rohit.muneshwar1406@gmail.com  
LinkedIn Profile: [Click Here](https://www.linkedin.com/in/rohit-muneshwar-a9079258/)  
Other Github repositories: [Click Here](https://github.com/rohit1406?tab=repositories)

---