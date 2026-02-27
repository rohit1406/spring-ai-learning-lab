# Spring Boot Revisited
## Introduction

SpringBoot 3.x framework (latest: 4.0.2) 
- Based on Spring Framework 6 (latest: 7.0.3)
- minimum JDK17 or above
- open source framework, used to develop faster and both Java based and web based appl, it is not a replacement of Spring framework, but it provides a production ready appl so as a developer we are focussing only on business logic rathen than doing all external configuration and downloading all jar files
- developed by Pivotal team

EJB - lot's of code
Struct, Hibernate - Single Tier
Spring - Complete application but lot's of boilerplate code, version conflicts
Spring Boot - automate above, auto-configuration, faster development, java standalone, web etc
Not a replacement of Spring Framework
Developed by Pivotal team

logging framework - log4j, slf4j

### Advantage
1. easy to understand, develop faster appl so increase productivity
2. reduce ur development time
3. Avoid xml configuration
4. Everything is autoconfigured, so no need for manual configuration
5. Springboot comes with embedded server like Apache Tomcat(default), Jetty, Undertow
6. Springboot comes with in-memory database called H2 database, used for testing purpose
7. Springboot comes with in-built logging framework like Logback, Slf4j

### Disadvantage
1. Migration effort - converting from existing spring to springboot project is not straight forward
2. Deploying springboot appl on other servers like Jboss, weblogic etc are not straight forward
3. Developed springboot appl by keeping Microservice and cloud in mind


## Springboot dependency management
- spring-boot-starter-parent is a project starter provide all basic configuration (jar files) to develop springboot appl
- manage dependencies and configuration in single place
- avoid version conflict
- dependency section tells maven what jar files to be downloaded, and parent section tells what version of jar file to be downloaded

## Developing Springboot appl - 3 ways
1. Using spring initalzr https://start.spring.io/
2. Using STS(Spring tool Suite) - IDE for Springboot
3. Using Spring CLI(Command Line Interface)

## Springboot annotations
1. Spring core annotation
    1. @Bean
    2. @Configuration
    3. @Autowired
    4. @Qualifier
    5. @Scope
    6. @Component
    7. @Controller, @Service, @Repository

2. Spring MVC annotation
- client request 
- controller(used to handle request and response using @Controller/@RestController) 
- service(used to write business logic using @Service)
- Repository(@Repository) 
- Database

1. @Controller - return response in view page (ie) JSP or Thymeleaf

2. @RestController - @Controller + @ResponseBody - return response in the form of JSON or string format

3. @RequestMapping 
- used to map the request to particular method logic 
- both class level and method level
````
   @RequestMapping(value="/fetchEmployee',method=RequestMethod.GET) - used to give GET request
   @RequestMapping(value="/insertEmployee',method=RequestMethod.POST) - used to give POST request
   @RequestMapping(value="/updateEmployee/{empid}',method=RequestMethod.PUT) - used to give PUT request
   @RequestMapping(value="/deleteEmployee?id=1000',method=RequestMethod.DELETE) - used to give DELETE request
````
4. @GetMapping, @PostMapping, @PutMapping, @DeleteMapping 
- used only at method level

5. @RequestBody 
- used to read entire input data in JSON format

6. @PathVariable 
- used to get value from the request url

@RequestMapping(value="/updateEmployee/{empid}/{ename}/{eage}',method=RequestMethod.PUT)

http://localhost:8080/projectname/updateEmployee/1000/Ram/25

7. @RequestParam 
- used to get value from the parameter that is passed between ? and & (query string)

http://localhost:8080/projectname/updateEmployee?id=1000&name=Ram&age=25

8. @ResponseBody 
- return response in JSON format

9. @ResponseEntity 
- return JSON response along with HTTP status code, status msg, header etc

3. Springboot annotation 
@SpringbootApplication 
- @EnableAutoConfiguration + @Configuration + @ComponentScan


Spring Web  
- Build web, including RESTful, applications using Spring MVC
- Uses Apache Tomcat as the default embedded container.

Spring Boot DevTools Developer Tools 
- Provides fast application restarts, LiveReload, and configurations for enhanced development experience.

Lombok Developer Tools 
- Java annotation library which helps to reduce boilerplate code.

CommandLineRunner interface
- used to execute any task immediately after the springboot appl is started
- public void run(String...s) {
}

ApplicationRunner interface
- used to execute any task immediately after the springboot appl is started
- public void run(ApplicationArgument arg) {
}

## Loading Properties
- By default springboot will read all configuration from application.properties or application.yml present inside src/main/resources folder.
- But if we create the properties file in different name and in different location, then how springboot can read those properties file

@PropertySource 
- used to read single properties file in different name and in different location

@PropertySources 
- used to read multiple properties file in different name and in different location


@Value 
- used to read single property from properties file to controller prg, it uses Spring Expression language

@ConfigurationProperties 
- used to map entire properties from properties file to separate java bean object


student.properties inside src/main/resources
````
student.id=20
student.name=Ram
student.address=Chennai
student.age=25
````
student1.properties inside c:/Training
````
student.email=ram@gmail.com
student.course=CSE
student.mark=89
````

````
@Configuration
//@PropertySource("classpath:student.properties")
//@PropertySource("file:\\C:\\Training\\student1.properties")
@PropertySources({
@PropertySource("classpath:student.properties"),
@PropertySource("file:\\C:\\Training\\student1.properties")
})
@ConfigurationProperties(prefix="student")
@Data  //@Getter+@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentConfig {
private String name;
private String address;
private Integer age;
private String email;
private String course;
}
````

@Value
1. Accessing properties one by one
2. Support SpEl
3. Loose binding/Loose Grammar is not supported (ie) property name should be matching
4. Validation of properties is not supported
5. support only scalar datatype

@ConfigurationProperties
1. Bulk injection of properties
2. Dosent Support SpEl
3. Loose binding/Loose Grammar is supported (ie) property name should be matching but we can also change the property only with special char and cases
4. Validation of properties is supported
5. support all datatypes as well as objects

To validate the properties we have to provide

    <dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
		</dependency>

@Validated 
- used to validate the properties

@Valid 
- used to validate the properties of nested class

````
mail.properties
#scalar datatypes
mail.to=abc@gmail.com
mail.from=xyz@gmail.com
mail.age=25
mail.first-name=Ram
mail.lastname=kumar
mail.middlename=T

#Complex datatype
mail.cc=efg@gmail.com,pqr@gmail.com
mail.bcc=uvw@gmail.com,mno@gmail.com

#Nested datatype
mail.credential.username=Ram
mail.credential.password=abcd


@Configuration
@PropertySource("classpath:mail.properties")
@ConfigurationProperties(prefix="mail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Validated
public class MailConfig {
@NotNull
private String to;
@NotNull
private String from;
@Max(value=40)
@Min(value=20)
private Integer age;

	@NotNull
    private String firstname;  //loose binding
	@NotNull
    private String LASTNAME; //loose binding
	@NotNull
    private String middle_name; //loose binding
    
    private String[] cc;
    private List<String> bcc;
    
    @Valid
    private Credential credential=new Credential();
    
    @Data
    public class Credential {
    	@NotNull
    	private String username;
    	@Size(min=4,max=8)
    	private String password;
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