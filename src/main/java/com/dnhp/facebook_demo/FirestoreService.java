package com.dnhp.facebook_demo;

import com.fasterxml.jackson.databind.MapperFeature;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FirestoreService {
	public final Firestore db;

	public FirestoreService() {
		db = FirestoreClient.getFirestore();
	}

	public void putReceivedMessage(String senderId, String recipientId, String messageText, long timestamp)
			throws ExecutionException, InterruptedException {
		// Create the message data
		Map<String, Object> messageData = new HashMap<>();
		messageData.put("messageText", messageText);
		messageData.put("timestamp", timestamp);
		messageData.put("senderID", senderId);
		messageData.put("recipientID", recipientId);

		DocumentSnapshot senderDoc = db.collection("message").document(senderId).get().get();
		long count;
		if (senderDoc.exists()) {
			Long conversationCount = senderDoc.getLong("conversationCount");
			count = conversationCount;
		} else {
			// If the document does not exist, set count to 1
			count = 1;
		}
		DocumentSnapshot conversationMetadataSnapshot = db.collection("message").document(senderId)
				.collection(String.valueOf(count)).document("conversation_metadata").get().get();
		Long lastMessageTimestamp = conversationMetadataSnapshot.getLong("lastMessageTimestamp");
		if (lastMessageTimestamp != null && (timestamp - lastMessageTimestamp) > 60 * 60 * 24 * 1000) // New
																										// conversation
																										// if the last
																										// message was
																										// sent more
																										// than 24 hour
																										// ago
		{
			count += 1;
			senderDoc.getReference().update("conversationCount", count);
			Logger.getGlobal().info("New conversation started");
		}

		// Save the message data to Firestore
		db.collection("message").document(senderId).collection(String.valueOf(count))
				.document(String.valueOf(timestamp)).set(messageData);

		// Update the conversationCount in the senderId document
		Map<String, Object> senderData = new HashMap<>();
		senderData.put("conversationCount", count);
		db.collection("message").document(senderId).set(senderData);

		// // Get the metadata document
		DocumentSnapshot metadataDoc = db.collection("message").document(senderId).collection(String.valueOf(count))
				.document("conversation_metadata").get().get();
		//
		// Create or update the metadata
		Map<String, Object> metadata = new HashMap<>();
		if (metadataDoc.exists()) {
			metadata = metadataDoc.getData();
		} else {
			metadata.put("firstMessageTimestamp", timestamp);
		}
		metadata.put("lastMessage", messageText);
		metadata.put("lastMessageTimestamp", timestamp);
		metadata.put("advisorId", null);

		// Save the metadata to Firestore
		db.collection("message").document(senderId).collection(String.valueOf(count)).document("conversation_metadata")
				.set(metadata);

	}

	public void putAdvisor(String advisorId, String name, String status)
			throws ExecutionException, InterruptedException {
		// Create the advisor data
		Map<String, Object> advisorData = new HashMap<>();
		advisorData.put("name", name);
		advisorData.put("status", status);

		// Save the advisor data to Firestore
		db.collection("advisors").document(advisorId).set(advisorData);
	}


	public DocumentSnapshot getLastMessage(String userId) throws ExecutionException, InterruptedException {
		return db.collection(userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get().get()
				.getDocuments().get(0);
	}

	public Map<String, Map<String, Object>> getMessages(String userID) throws ExecutionException, InterruptedException {
		// Create a List and add elements to it in the order you want
		List<Map.Entry<String, Map<String, Object>>> list = new ArrayList<>();
		List<QueryDocumentSnapshot> conversations = db.collection("message").document(userID).collection("1").get()
				.get().getDocuments();
		for (QueryDocumentSnapshot conversation : conversations) {
			String conversationId = conversation.getId();
			if (conversationId.equals("conversation_metadata")) {
				continue;
			}
			Map<String, Object> conversationMessages = new HashMap<>();

			// Get all fields for this conversation
			Map<String, Object> fields = conversation.getData();
			String messageText = (String) fields.get("messageText");
			Long timestamp = (Long) fields.get("timestamp");
			String senderID = (String) fields.get("senderID");
			String recipientID = (String) fields.get("recipientID");


			// Add these fields to the conversation messages
			conversationMessages.put("messageText", messageText);
			conversationMessages.put("timestamp", timestamp);
			conversationMessages.put("senderID", senderID);
			conversationMessages.put("recipientID", recipientID);

			// Add the conversation messages to the list
			list.add(new AbstractMap.SimpleEntry<>(conversationId, conversationMessages));
		}

		// Convert the List to a Map
		Map<String, Map<String, Object>> allMessages = new LinkedHashMap<>();
		for (Map.Entry<String, Map<String, Object>> entry : list) {
			allMessages.put(entry.getKey(), entry.getValue());
		}
		// Logger.getGlobal().info("All messages: " + allMessages);
		return allMessages;
	}

	public ResponseEntity<String> sendMessage(String recipientId, Map<String, Object> message) throws IOException, InterruptedException {
		// Create the message data
		Map<String, Object> messageData = new HashMap<>();
		messageData.put("messageText", message.get("messageText"));
		messageData.put("timestamp", message.get("timestamp"));
		messageData.put("senderID", message.get("senderID"));
		messageData.put("recipientID", recipientId);

		// Save the message data to Firestore
		db.collection("message").document(recipientId).collection("1")
				.document(String.valueOf(message.get("timestamp"))).set(messageData);


		// Post message to Facebook
		var url = "https://graph.facebook.com/v18.0/me/messages?access_token=EABsLirhuG9kBO1kHgWn5AecLhNPksgVKogL72F4oB8sCZB9roIZC02Uxv4IngGG0SZCJzseTeBwaJSyKK43ZAkZC5oR3Tg3iu3VSJGxl1c3VhFAFbIrBzWi1Cqt4gljsbIPJpxyXJsXKGw1QIVNgunF2d755bOXyqQ9FjZA17dyb5yUZC1eusvn7RL2orzrbT4ZD";
		
		String data = "{"
			+ "\"message\": {"
				+ "\"text\": \"" + message.get("messageText") + "\""
			+ "},"
			+ "\"messaging_type\": \"RESPONSE\","
			+ "\"recipient\": {"
				+ "\"id\": \"" + recipientId + "\""
			+ "}"
		+ "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("Message sent successfully");
            return ResponseEntity.status(HttpStatus.OK).body("{\"message\":\"Message sent successfully\"}");
        } else {
            System.out.println("Failed to send message: " + response.body());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\":\"Failed to send message\"}");
        }
	}
}
