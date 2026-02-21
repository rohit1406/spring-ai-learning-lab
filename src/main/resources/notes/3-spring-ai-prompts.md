# Prompt Templates

### Chat Options
1. Temperature - creativity vs predictability
   - low (0.0 - 0.3) - very deterministic
   - medium (0.4 - 0.7) - balanced
   - high (0.8 - 1.5) - creative

2. Maxtokens - maximum length of response
    - Token = words+punctuation
    - Maxtokens=50 - short, incomplete explanation
    - Maxtokens=200 - clear explanation
    - Maxtokens=1000 - very detailed
   - Too high = cost + latency - no


3. TopP - Nucleus sampling
    - control word choices
      - 1.0 - consider all possible words
      - 0.9 - consider top 90% probability words (DEFAULT)
      - 0.5 - very narrow, safe words only
      - Prompt: Describe a sunset

4. Frequency Penalty
    - control repetition of same words
    - Higher frequency - less repetition
    - 0.3 to 0.6 for explanation - default is 0.4

5. Presence Penalty
    - encourage with new topics/ideas 
    - control repetition of same concepts
    - Higher presencepenalty - new ideas
   - Prompt: Explain Java
   - PresencePenalty = 0.0 - basics of Java
   - PresencePenalty= 0.2 (default) - new features of Java

#### Balanced defaults
- Temperature = 0.6 - balanced
- Maxtokens = 200 - controlled response size
- topP = 0.9 - good vocabulary
- frequencyPenalty - 0.4 - avoids repetition
- presencePenalty - 0.2 - introduce new


### PromptTemplate class
- parameterized prompt with placeholders, passing prompt at runtime
````
@GetMapping("/popular")
public String findPopularYoutubers(@RequestParam(value="genre",defaultValue="tech") String genre) {
String message = """
List most popular Youtubers in {genre} along with their current subscribers counts. If you dont know'
the answer, just say "I dont know".
""";

		PromptTemplate pt=new PromptTemplate(message);
		Prompt p=pt.create(Map.of("genre",genre));
		return chatClient.call(p).getResult().getOutput().getText();
		
	}
````

- Instead of creating same prompt multiple times, we can access it from the files (.st extension- String Template)
    - Inside resources 
    - create prompts folder 
    - create popular.st file
    - Prompt Content: List most popular Youtubers in {genre} along with their current subscribers counts. If you dont know the answer, just say "I dont know".
````
@Value("classpath:/prompts/popular.st")
private Resource promptResource;

	@GetMapping("/popular")
    public String findPopularYoutubers(@RequestParam(value="genre",defaultValue="tech") String genre) {	
		PromptTemplate pt=new PromptTemplate(promptResource);
		Prompt p=pt.create(Map.of("genre",genre));
		return chatClient.call(p).getResult().getOutput().getText();
		
	}
````

### OutputConverters/OutputParsers
- Used to structure the response from LLM
- 3 implementations

1. ListOutputConverter - structure the response as List
````
@GetMapping("/countries")
public List<String> getCountries(@RequestParam(value="country",defaultValue="India") String country) {
var message="""
Please give me a list of destinations for the country {country}. If you dont know the answer just say "I dont know".
{format}
""";
ListOutputConverter output=new ListOutputConverter(new DefaultConversionService());
PromptTemplate pt=new PromptTemplate(message);
Prompt p=pt.create(Map.of("country",country,"format",output.getFormat()));
ChatResponse response=chatClient.call(p);
return output.convert(response.getResult().getOutput().getText());

	}
````

2. MapOutputConverter - structure the response as Map
````
@GetMapping("/authors/{author}")
public Map<String,Object> getAuthors(@PathVariable("author") String author) {
var message="""
Generate list of links for the author {author}. Include the author name as the key and any social network links as the value. If you dont know the answer just say "I dont know".
{format}
""";

    	MapOutputConverter map=new MapOutputConverter();
    	String format=map.getFormat();
    	PromptTemplate pt=new PromptTemplate(message);
    	Prompt p=pt.create(Map.of("author",author,"format",format));
    	ChatResponse response=chatClient.call(p);
		return map.convert(response.getResult().getOutput().getText()); 
	}
````

3. BeanOutputConverter
````
public record Author(String author, List<String> books) {

}

@GetMapping("/byAuthor")
public Author getAuthorByBook(@RequestParam(value="author",defaultValue="Ken Kousen") String author) {
var message="""
Generate a list of books written by the author {author}. If you arent positive that the book belong to this author please dont include it.
{format}
""";
BeanOutputConverter<Author> output=new BeanOutputConverter<Author>(Author.class);
String format=output.getFormat();
PromptTemplate pt=new PromptTemplate(message);
Prompt p=pt.create(Map.of("author",author,"format",format));
ChatResponse response=chatClient.call(p);
return output.convert(response.getResult().getOutput().getText());
}
````

### 3 types of Prompt
1. Zero shot prompting
    - You ask the model to do a task without giving any examples. The model relies only on the pretrained data

When?
- Task is simple or common
- you want quick results
- No strict output format required

Eg:
Prompt: Explain REST API in simple terms
````
@GetMapping("/explain")
public String message() {
var msg=new UserMessage("Explain kafka in simple terms");
return chatClient.call(new Prompt(msg)).getResult().getOutput().getText();
}
````

2. Few shot prompting
    - You provide few examples of input and output and then ask the model to do similar task

When?
1. consistent format
2. domain specific

````
@GetMapping("/explain1")
public String message1() {
String msg="""
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
        
   		return chatClient.call(new Prompt(msg)).getResult().getOutput().getText();
   	}
````

3. Chain of Thoughts(CoT) prompting
   - You ask the model to show its reasoning steps, not just the final answer

When?
- Logical reasoning
- Debugging
- Math/decision making
````
  @GetMapping("/explain2")
  public String message2() {
  var message=new UserMessage("""
  An application is slow during peak hours.
  Think step by step and identify possible causes.
  """);
  return chatClient.call(new Prompt(message)).getResult().getOutput().getText();
  }
````


