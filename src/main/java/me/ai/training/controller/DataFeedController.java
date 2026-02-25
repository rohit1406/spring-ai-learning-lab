package me.ai.training.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import me.ai.training.exceptions.DataStoreException;
import me.ai.training.service.DataFeedService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @author Rohit Muneshwar
 * @created on 2/18/2026
 *
 *
 */
@Tag(name = "Test Data Storage")
@RestController
@RequestMapping("/data-feed")
@Slf4j
public class DataFeedController {
    private final DataFeedService dataFeedService;
    public DataFeedController(DataFeedService dataFeedService){
        this.dataFeedService = dataFeedService;
    }

    @Operation(summary = "save Java related documentation data to Vector Store", description = """
            Use this method to ONLY save the test data to vector store.
            """)
    @PostMapping("/save-java-test-data")
    public ResponseEntity<String> saveTestData(){
        try {
            dataFeedService.saveJavaTestData();
            return ResponseEntity.ok("Data saved successfully");
        }catch(DataStoreException ex){
            log.error("Exception occurred:", ex);
            return ResponseEntity.status(HttpStatus.FOUND).body(ex.getMessage());
        } catch(Exception ex){
            log.error("Exception occurred:", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Data could not be saved: "+ex.getMessage());
        }
    }

    @Operation(summary = "save Big Bunny Video Subtitle data to Vector Store", description = """
            Use this method to ONLY save the test data to vector store.
            This data is related to the Big Bunny Video subtitle.
            """)
    @PostMapping("/save-big-bunny-subtitle-test-data")
    public ResponseEntity<String> saveBigBunnySubtitleData(){
        try {
            dataFeedService.saveBigBunnySubtitleData();
            return ResponseEntity.ok("Data saved successfully");
        }catch(DataStoreException ex){
            log.error("Exception occurred:", ex);
            return ResponseEntity.status(HttpStatus.FOUND).body(ex.getMessage());
        } catch(Exception ex){
            log.error("Exception occurred:", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Data could not be saved: "+ex.getMessage());
        }
    }

    @Operation(summary = "save data from files to Vector Store", description = """
            Use this method to ONLY save the test data to vector store.
            This is a test data present in resources folder with file names file1.docx, file2.docx, file3.docx.
            """)
    @PostMapping("/save-employee-project-test-data")
    public ResponseEntity<String> saveEmployeeProjectTestData(){
        try {
            dataFeedService.validateData("John Doh is assigned to Project Alpha");
            dataFeedService.validateData("Project Alpha has 100 Euro budget");
            dataFeedService.validateData("Project Alpha has 15 employees");
            log.info("Saving data");
            //dataFeedService.ingestDocument(new ClassPathResource("docs/file1.docx"), "DOC1", "ORG");
            //dataFeedService.ingestDocument(new ClassPathResource("docs/file2.docx"), "DOC2", "ORG");
            //dataFeedService.ingestDocument(new ClassPathResource("docs/file3.docx"), "DOC3", "ORG");
            dataFeedService.ingestTestDocumentWithReferences(new ClassPathResource("docs/file1.docx"), "DOC1", "ORG");
            dataFeedService.ingestTestDocumentWithReferences(new ClassPathResource("docs/file2.docx"), "DOC2", "ORG");
            dataFeedService.ingestTestDocumentWithReferences(new ClassPathResource("docs/file3.docx"), "DOC3", "ORG");
            return ResponseEntity.ok("Data saved successfully");
        }catch(DataStoreException ex){
            log.error("Exception occurred:", ex);
            return ResponseEntity.status(HttpStatus.FOUND).body(ex.getMessage());
        } catch(Exception ex){
            log.error("Exception occurred:", ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Data could not be saved: "+ex.getMessage());
        }
    }
}
