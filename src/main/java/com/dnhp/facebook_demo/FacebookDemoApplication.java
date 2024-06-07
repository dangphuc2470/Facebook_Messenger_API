package com.dnhp.facebook_demo;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.json.JSONObject;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
    public String test() {
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
    public String callback(@RequestBody String input) throws ExecutionException, InterruptedException
    {
        LOGGER.info("requestBody:" + input);
        latestMessage = input;

        // Parse the input string to JSON
        JSONObject jsonObject = new JSONObject(input);

        JSONObject firstEntry = jsonObject.getJSONArray("entry").getJSONObject(0);
        //long time = firstEntry.getLong("time");
        //String entryId = firstEntry.getString("id");

        JSONObject firstMessaging = firstEntry.getJSONArray("messaging").getJSONObject(0);
        String senderId = firstMessaging.getJSONObject("sender").getString("id");
        String recipientId = firstMessaging.getJSONObject("recipient").getString("id");
        long timestamp = firstMessaging.getLong("timestamp");
        //String mid = firstMessaging.getJSONObject("message").getString("mid");
        String messageText = firstMessaging.getJSONObject("message").getString("text");

        // Save the message to Firestore
        firestoreService.putReceivedMessage(senderId, recipientId, messageText, timestamp);
        return "OK";
    }

    @PostMapping("/send-message/{recipientId}/{conversationNum}")
    public ResponseEntity<String> sendMessage(@PathVariable String recipientId, @PathVariable String conversationNum, @RequestBody Map<String, Object> message) throws IOException, InterruptedException
    {
        return firestoreService.sendMessage(recipientId, message, conversationNum);
    }


    @GetMapping("/get-messages/{senderId}/{conversationNum}")
public Map<String, Map<String, Object>> getMessages(@PathVariable String senderId, @PathVariable String conversationNum) throws ExecutionException, InterruptedException {
        LOGGER.info("Getting messages for senderId: " + senderId + " and conversationNum: " + conversationNum);
        return firestoreService.getMessages(senderId, conversationNum);
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

    @GetMapping("/sse")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter();
        DocumentReference docRef = firestoreService.db.collection("message").document("25240652615526181");
        docRef.addSnapshotListener((snapshot, e) ->
        {
            LOGGER.info("Listening for changes");
            if (e != null)
            {
                System.err.println("Listen failed: " + e);
                return;
            }

            if (snapshot != null && snapshot.exists())
            {
                try
                {
                    // Send the update to the client-side
                    emitter.send(SseEmitter.event().name("message").data(snapshot.getData()));
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            } else
            {
                System.out.print("Current data: null");
            }
        });
    
        return emitter;
    }



}


