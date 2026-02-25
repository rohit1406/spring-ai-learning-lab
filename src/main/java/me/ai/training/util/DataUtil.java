package me.ai.training.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Rohit Muneshwar
 * @created on 2/19/2026
 *
 *
 */
public class DataUtil {

    public static final String BIG_BUCK_BUNNY_SRT = """
            1
            00:00:01,000 --> 00:00:04,000
            This is a demonstration of a free
            subtitle file.
            
            2
            00:00:04,500 --> 00:00:07,000
            It is 100 lines long.
            
            3
            00:00:08,000 --> 00:00:10,500
            [Upbeat music playing]
            
            4
            00:00:12,000 --> 00:00:14,000
            The bunny wakes up.
            
            5
            00:00:14,100 --> 00:00:16,000
            He stretches his arms.
            
            6
            00:00:16,500 --> 00:00:18,500
            What a beautiful morning!
            
            7
            00:00:19,000 --> 00:00:21,000
            He looks around the forest.
            
            8
            00:00:21,500 --> 00:00:23,500
            Everything is so peaceful.
            
            9
            00:00:24,000 --> 00:00:27,000
            Until he hears a loud sound.
            
            10
            00:00:27,500 --> 00:00:29,500
            *Thump* *Thump* *Thump*
            
            11
            00:00:30,000 --> 00:00:33,000
            Here come the little rodents.
            
            12
            00:00:33,500 --> 00:00:35,500
            They are playing tricks again.
            
            13
            00:00:36,000 --> 00:00:38,000
            They throw a nut at him.
            
            14
            00:00:38,500 --> 00:00:40,500
            *Bonk*
            
            15
            00:00:41,000 --> 00:00:43,000
            Big Buck Bunny is not amused.
            
            16
            00:00:43,500 --> 00:00:45,500
            He decides to take action.
            
            17
            00:00:46,000 --> 00:00:48,000
            He starts planning a trick.
            
            18
            00:00:48,500 --> 00:00:50,500
            A very funny trick.
            
            19
            00:00:51,000 --> 00:00:53,000
            He picks up some berries.
            
            20
            00:00:53,500 --> 00:00:55,500
            And some sticky sap.
            
            21
            00:00:56,000 --> 00:00:58,000
            This will be perfect.
            
            22
            00:00:58,500 --> 00:01:00,500
            The rodents come closer.
            
            23
            00:01:01,000 --> 00:01:03,000
            They look very innocent now.
            
            24
            00:01:03,500 --> 00:01:05,500
            But they are not.
            
            25
            00:01:06,000 --> 00:01:08,000
            Bunny hides behind a tree.
            
            26
            00:01:08,500 --> 00:01:10,500
            Waiting for the right moment.
            
            27
            00:01:11,000 --> 00:01:13,000
            *Wait for it...*
            
            28
            00:01:13,500 --> 00:01:15,500
            *Wait for it...*
            
            29
            00:01:16,000 --> 00:01:18,000
            Now!
            
            30
            00:01:18,500 --> 00:01:20,500
            He jumps out and scares them.
            
            31
            00:01:21,000 --> 00:01:23,000
            The rodents scream.
            
            32
            00:01:23,500 --> 00:01:25,500
            And run away fast.
            
            33
            00:01:26,000 --> 00:01:28,000
            Bunny laughs heartily.
            
            34
            00:01:28,500 --> 00:01:30,500
            He sits back down.
            
            35
            00:01:31,000 --> 00:01:33,000
            Enjoying the peace again.
            
            36
            00:01:33,500 --> 00:01:35,500
            The sun is setting now.
            
            37
            00:01:36,000 --> 00:01:38,000
            A beautiful sunset.
            
            38
            00:01:38,500 --> 00:01:40,500
            Colors of orange and purple.
            
            39
            00:01:41,000 --> 00:01:43,000
            He sighs with content.
            
            40
            00:01:43,500 --> 00:01:45,500
            What a day it has been.
            
            41
            00:01:46,000 --> 00:01:48,000
            A very, very long day.
            
            42
            00:01:48,500 --> 00:01:50,500
            He closes his eyes.
            
            43
            00:01:51,000 --> 00:01:53,000
            And falls asleep.
            
            44
            00:01:53,500 --> 00:01:55,500
            *Zzzzzzzzz*
            
            45
            00:01:56,000 --> 00:01:58,000
            *Zzzzzzzzz*
            
            46
            00:01:58,500 --> 00:02:00,500
            The forest is quiet.
            
            47
            00:02:01,000 --> 00:02:03,000
            Only the sound of crickets.
            
            48
            00:02:03,500 --> 00:02:05,500
            And the wind in the trees.
            
            49
            00:02:06,000 --> 00:02:08,000
            Goodnight, Big Buck Bunny.
            
            50
            00:02:08,500 --> 00:02:10,500
            Goodnight, forest.
            
            51
            00:02:11,000 --> 00:02:13,000
            This marks the halfway point.
            
            52
            00:02:13,500 --> 00:02:15,500
            Fifty lines down.
            
            53
            00:02:16,000 --> 00:02:18,000
            Fifty to go.
            
            54
            00:02:18,500 --> 00:02:20,500
            The story continues tomorrow.
            
            55
            00:02:21,000 --> 00:02:23,000
            When the sun comes up.
            
            56
            00:02:23,500 --> 00:02:25,500
            The birds will sing.
            
            57
            00:02:26,000 --> 00:02:28,000
            The flowers will open.
            
            58
            00:02:28,500 --> 00:02:30,500
            And the cycle starts again.
            
            59
            00:02:31,000 --> 00:02:33,000
            It is a simple life here.
            
            60
            00:02:33,500 --> 00:02:35,500
            But a good one.
            
            61
            00:02:36,000 --> 00:02:38,000
            No stress.
            
            62
            00:02:38,500 --> 00:02:40,500
            No worries.
            
            63
            00:02:41,000 --> 00:02:43,000
            Just nature.
            
            64
            00:02:43,500 --> 00:02:45,500
            And friendship.
            
            65
            00:02:46,000 --> 00:02:48,000
            (Well, mostly friendship).
            
            66
            00:02:48,500 --> 00:02:50,500
            Those rodents are trouble.
            
            67
            00:02:51,000 --> 00:02:53,000
            But they keep things interesting.
            
            68
            00:02:53,500 --> 00:02:55,500
            The stream is flowing.
            
            69
            00:02:56,000 --> 00:02:58,000
            The water is clear.
            
            70
            00:02:58,500 --> 00:03:00,500
            Fish are swimming.
            
            71
            00:03:01,000 --> 00:03:03,000
            A blue jay chirps.
            
            72
            00:03:03,500 --> 00:03:05,500
            It is a lovely day.
            
            73
            00:03:06,000 --> 00:03:08,000
            We are approaching line 75.
            
            74
            00:03:08,500 --> 00:03:10,500
            Keep watching.
            
            75
            00:03:11,000 --> 00:03:13,000
            The scene is changing.
            
            76
            00:03:13,500 --> 00:03:15,500
            Moving to the meadow.
            
            77
            00:03:16,000 --> 00:03:18,000
            Where the flowers bloom.
            
            78
            00:03:18,500 --> 00:03:20,500
            Yellow, red, and blue.
            
            79
            00:03:21,000 --> 00:03:23,000
            Bees are buzzing.
            
            80
            00:03:23,500 --> 00:03:25,500
            Collecting nectar.
            
            81
            00:03:26,000 --> 00:03:28,000
            Making honey.
            
            82
            00:03:28,500 --> 00:03:30,500
            It's a busy place.
            
            83
            00:03:31,000 --> 00:03:33,000
            The wind blows gently.
            
            84
            00:03:33,500 --> 00:03:35,500
            A soft breeze.
            
            85
            00:03:36,000 --> 00:03:38,000
            It feels refreshing.
            
            86
            00:03:38,500 --> 00:03:40,500
            Almost at the end now.
            
            87
            00:03:41,000 --> 00:03:43,000
            Line 90 coming up.
            
            88
            00:03:43,500 --> 00:03:45,500
            Hope you enjoyed this.
            
            89
            00:03:46,000 --> 00:03:48,000
            Subtitle practice!
            
            90
            00:03:48,500 --> 00:03:50,500
            Only ten lines left.
            
            91
            00:03:51,000 --> 00:03:53,000
            Nine, eight, seven...
            
            92
            00:03:53,500 --> 00:03:55,500
            ...six, five, four...
            
            93
            00:03:56,000 --> 00:03:58,000
            ...three, two, one...
            
            94
            00:03:58,500 --> 00:04:00,500
            Final line is here!
            
            95
            00:04:01,000 --> 00:04:03,000
            This is line 95.
            
            96
            00:04:03,500 --> 00:04:05,500
            Line 96.
            
            97
            00:04:06,000 --> 00:04:08,000
            Line 97.
            
            98
            00:04:08,500 --> 00:04:10,500
            Line 98.
            
            99
            00:04:11,000 --> 00:04:13,000
            Line 99.
            
            100
            00:04:13,500 --> 00:04:16,000
            And one hundred. Goodbye!
            
            """;
    public static final String WHAT_IS_JAVA = "Java is a platform-independent, object-oriented programming language.";
    public static final String BIG_BUNNY_SUBTITLE_CHUNK = "It is 100 lines long";
    public static List<String> getData(){
        return List.of(WHAT_IS_JAVA,
                "JVM converts Java bytecode into machine code at runtime.",
                "JDK includes the compiler (javac) and JVM tools.",
                "Java supports garbage collection to manage memory automatically.",
                "A class is a blueprint, while an object is an instance of that class.",
                "public static void main(String[] args) is the entry point of a Java program.",
                "Java supports multi-threading using the Thread class or Runnable interface.",
                "synchronized keyword ensures thread safety.",
                "Spring Boot simplifies Java backend development with auto-configuration.",
                "Hibernate ORM maps Java objects to database tables.",
                "JPA is a specification; Hibernate is an implementation.",
                "Java 8 introduced Lambda expressions and Streams API.",
                "The Optional class avoids null pointer exceptions.",
                "In Spring, @Autowired injects dependencies automatically.",
                "Spring Security handles authentication and authorization.",
                "Spring Data JPA provides repository interfaces for database queries.",
                "Microservices in Spring Boot often use Eureka for service discovery.",
                "Spring Cloud Config provides centralized configuration.",
                "Spring Boot applications typically run on an embedded Tomcat server.",
                "REST APIs in Java use @RestController and @RequestMapping.");
    }

    public static List<String> getBigBuckBunnySubtitle(){
        List<String> myList = Arrays.stream(BIG_BUCK_BUNNY_SRT.split("\\n\\s*\\n")).toList();
        int size = myList.size();
        int parts = 20;
        int partSize =  size / parts;
        List<String> chunks = IntStream.range(0, parts)
                .mapToObj(i -> myList.subList(i * partSize, (i+1) * partSize))
                .toList()
                .stream().map(x -> x.stream().collect(Collectors.joining("\n")))
                .toList();
        return chunks;
    }

}
