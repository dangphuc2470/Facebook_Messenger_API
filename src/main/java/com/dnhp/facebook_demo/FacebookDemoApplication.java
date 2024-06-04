package com.dnhp.facebook_demo;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.json.JSONObject;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@RestController
public class FacebookDemoApplication
{
    private FirestoreService firestoreService;
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookDemoApplication.class);

    public static void main(String[] args)
    {
        SpringApplication.run(FacebookDemoApplication.class, args);
    }

    @GetMapping("/test")
    public String test()
    {
        return "Test";
    }

    @GetMapping(value = "callback")
    public String callback(HttpServletRequest request, HttpServletResponse response)
    {
        LOGGER.info("callback:");
        String mode = request.getParameter("hub.mode");
        String challenge = request.getParameter("hub.challenge");
        String token = request.getParameter("hub.verify_token");
        LOGGER.info("mode:" + mode);
        LOGGER.info("challenge:" + challenge);
        LOGGER.info("token:" + token);
        if ("subscribe".equals(mode) && token != null && token.equals("dnhp_token"))
        {
            response.setHeader("content-type", "application/text");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setStatus(200);
        } else
        {
            response.setHeader("content-type", "application/text");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setStatus(403);
            challenge = "Invalid token";
        }
        return challenge;
    }

    private String latestMessage = "";

    @GetMapping(value = "latest-message")
    public String getLatestMessage()
    {
        String message = latestMessage;
        latestMessage = null;
        return message;
    }

    @PostMapping(value = "callback")
    public String callback(@RequestBody String input)
    {
        LOGGER.info("requestBody:" + input);
        latestMessage = input;

        // Parse the input string to JSON
        JSONObject jsonObject = new JSONObject(input);

        // Get the sender ID and message text
        String senderId = jsonObject.getJSONArray("entry").getJSONObject(0).getJSONArray("messaging").getJSONObject(0).getJSONObject("sender").getString("id");
        String messageText = jsonObject.getJSONArray("entry").getJSONObject(0).getJSONArray("messaging").getJSONObject(0).getJSONObject("message").getString("text");

        // Save the message to Firestore
        firestoreService.sendMessage(senderId, messageText, "Facebook");

        return "OK";
    }

    @GetMapping("/hello")
    public String sayHello(@RequestParam(value = "myName", defaultValue = "World") String name)
    {
        return String.format("Hello %s!", name);
    }


    @PostConstruct
    public void init()
    {
        try
        {
            FileInputStream serviceAccount = new FileInputStream("chatapp-d4662-firebase-adminsdk-4zh1x-e6458f015b.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty())
            {
                FirebaseApp.initializeApp(options);
            }
            firestoreService = new FirestoreService();
        } catch (IOException e)
        {
            e.printStackTrace();
            LOGGER.error("Error initializing Firebase app");
        }
    }

    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> body)
    {
        String message = body.get("message");
        if (message != null)
        {
            // Replace "userId" and "senderId" with the actual user ID and sender ID
            firestoreService.sendMessage("userId", message, "senderId");
            LOGGER.info(message);
            return ResponseEntity.ok("Message sent");
        } else
        {
            return ResponseEntity.badRequest().body("No message found in request body");
        }
    }
}



