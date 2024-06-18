package com.dnhp.facebook_demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

@Service
public class FirestoreService {
	public final Firestore db;

	public FirestoreService() {
		db = FirestoreClient.getFirestore();
	}


	/// Region chat received message App Engine
	public void putReceivedMessage(String senderId, String recipientId, String messageText, long timestamp, RestTemplate restTemplate)
			throws ExecutionException, InterruptedException {
		Boolean isNeedToUpdateMessage = true;
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
		if (lastMessageTimestamp != null && (timestamp - lastMessageTimestamp) > 10 * 1000) // New
		// conversation
		// if the last
		// message was
		// sent more
		// than 30m
		// ago
		{
			count += 1;
			senderDoc.getReference().update("conversationCount", count);
			Logger.getGlobal().info("New conversation started");
			findAdvisor(senderId, count);
			isNeedToUpdateMessage = false;
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

		// Create or update the metadata
		Map<String, Object> metadata = new HashMap<>();
		if (metadataDoc.exists()) {
			metadata = metadataDoc.getData();
		} else {
			metadata.put("firstMessageTimestamp", timestamp);
		}
		metadata.put("lastMessage", messageText);
		metadata.put("lastMessageTimestamp", timestamp);
		metadata.put("lastSenderID", senderId);
		metadata.put("conversationID", senderId);


		// Save the metadata to Firestore
		db.collection("message").document(senderId).collection(String.valueOf(count)).document("conversation_metadata")
				.set(metadata);
		if (isNeedToUpdateMessage)
		{
			String response = restTemplate.getForObject("https://trusted-badger-fairly.ngrok-free.app/trigger-reload", String.class);
			Logger.getGlobal().info(response);
		}
		else
		{
			String response = restTemplate.getForObject("https://trusted-badger-fairly.ngrok-free.app/trigger-conversation", String.class);
			Logger.getGlobal().info(response);

		}


	}

//	public void putAdvisor(String advisorId, String name, String status)
//			throws ExecutionException, InterruptedException {
//		// Create the advisor data
//		Map<String, Object> advisorData = new HashMap<>();
//		advisorData.put("name", name);
//		advisorData.put("status", status);
//
//		// Save the advisor data to Firestore
//		db.collection("advisors").document(advisorId).set(advisorData);
//	}


	// public DocumentSnapshot getLastMessage(String userId) throws ExecutionException, InterruptedException {
	// 	return db.collection(userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get().get()
	// 			.getDocuments().get(0);
	// }

	public void findAdvisor(String senderId, long count) throws ExecutionException, InterruptedException
	{
		// Find the last advisor of conversation
		if (count > 1)
		{
			// Get the advisorId of the last conversation
			DocumentSnapshot advisorSnapshot = db.collection("message").document(senderId).collection(String.valueOf(count - 1)).document("conversation_metadata").get().get();
			String advisorId = advisorSnapshot.getString("advisorId");
			// Delete the advisor from the senderId document
			db.collection("message").document(senderId).collection(String.valueOf(count -1 )).document("conversation_metadata")
					.update("advisorId", FieldValue.delete());
			// Decrement the conversation count for the advisor
			if (advisorId != null)
			{
				db.collection("advisors").document(advisorId).update("conversationCount", FieldValue.increment(-1));
			}
		}

		// Get the advisor with the least number of conversations
		DocumentSnapshot advisorSnapshot = db.collection("advisors").orderBy("conversationCount").limit(1).get().get()
				.getDocuments().get(0);
		// Get the advisor name
		String advisorId = advisorSnapshot.getId();
		// Set the advisor name to the senderId document
		db.collection("message").document(senderId).collection(String.valueOf(count)).document("conversation_metadata")
				.update("advisorId", advisorId);
		// Increment the conversation count for the advisor
		db.collection("advisors").document(advisorId).update("conversationCount", FieldValue.increment(1));

	}

	public void putAdvisor(String advisorId, String name, String status)
	{
		// Create the advisor data
		Map<String, Object> advisorData = new HashMap<>();
		advisorData.put("name", name);
		advisorData.put("status", status);
		advisorData.put("conversationCount", 0);
		// Save the advisor data to Firestore
		db.collection("advisors").document(advisorId).set(advisorData);
	}
	 /// End region chat received message App Engine

	public List<Map<String, Object>> getConversation(String conversationId)
			throws Exception {
		var count = 0;
		DocumentSnapshot documentSnapshot = db.collection("message").document(conversationId).get().get();
		if (documentSnapshot.exists()) {
			Long conversationCount = documentSnapshot.getLong("conversationCount");
			if (conversationCount != null) {
				count = conversationCount.intValue();
			} else {
				throw new IllegalStateException("conversationCount is null");
			}
		} else {
			throw new IllegalStateException("Document does not exist");
		}

		List<Map<String, Object>> conversations = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			DocumentReference docRef = db.collection("message").document(conversationId).collection(String.valueOf(i)).document("conversation_metadata");
			DocumentSnapshot docSnapshot = docRef.get().get();
			if (docSnapshot.exists()) {
				Map<String, Object> conversationData = docSnapshot.getData();
				assert conversationData != null;
				conversationData.put("conversationNum", String.valueOf(i));
				conversations.add(conversationData);
			}
		}

		conversations.sort((conversation1, conversation2) -> {
			Long timestamp1 = (Long) conversation1.get("lastMessageTimestamp");
			Long timestamp2 = (Long) conversation2.get("lastMessageTimestamp");

			// Handle potential null values
			if (timestamp1 == null && timestamp2 == null) {
				return 0;
			} else if (timestamp1 == null) {
				return 1;
			} else if (timestamp2 == null) {
				return -1;
			}

			// Compare timestamps
			return timestamp2.compareTo(timestamp1);
		});


		for (Map<String, Object> conversation : conversations) {
			String conversationNum = (String) conversation.get("conversationNum");
			String conversationLastTimestamp = conversation.get("lastMessageTimestamp").toString();
			Logger.getGlobal().info("Conversation: " + conversationNum + " Last message timestamp: " + conversationLastTimestamp);
		}

		return conversations;
	}
	public Map<String, Map<String, Object>> getMessages(String userID,
														String conversationNum)
			throws ExecutionException, InterruptedException {
		List<Map.Entry<String, Map<String, Object>>> list = new ArrayList<>();
		List<QueryDocumentSnapshot> conversations = db.collection("message").document(userID).collection(conversationNum).get()
				.get().getDocuments();
		for (QueryDocumentSnapshot conversation : conversations) {
			String conversationId = conversation.getId();
			if (conversationId.equals("conversation_metadata")) {
				continue;
			}
			Map<String, Object> conversationMessages = getStringObjectMap(conversation);

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
	public ResponseEntity<String> sendMessage(String recipientId,
											  Map<String, Object> message,
											  String conversationNum)
			throws IOException, InterruptedException, ExecutionException {
		// Create the message data
		Map<String, Object> messageData = new HashMap<>();
		messageData.put("messageText", message.get("messageText"));
		messageData.put("timestamp", message.get("timestamp"));
		messageData.put("senderID", message.get("senderID"));
		messageData.put("recipientID", recipientId);

		// Save the message data to Firestore
		db.collection("message").document(recipientId).collection(conversationNum)
				.document(String.valueOf(message.get("timestamp"))).set(messageData);

		// Get the metadata document
		DocumentSnapshot metadataDoc = db.collection("message").document(recipientId).collection(conversationNum)
				.document("conversation_metadata").get().get();

		// Create or update the metadata
		Map<String, Object> metadata = new HashMap<>();
		if (metadataDoc.exists()) {
			metadata = metadataDoc.getData();
		} else {
			metadata.put("firstMessageTimestamp", message.get("timestamp"));
		}
		assert metadata != null;
		metadata.put("lastMessage", message.get("messageText"));
		metadata.put("lastMessageTimestamp", message.get("timestamp"));
		metadata.put("lastSenderID", message.get("senderID"));
		metadata.put("conversationID", recipientId);
		// Todo: Remove hardcoding
		metadata.put("advisorId", "AD1");

		Logger.getGlobal().info("Metadata: " + metadata);
		// Save the metadata to Firestore




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
			db.collection("message").document(recipientId).collection(conversationNum).document("conversation_metadata")
					.set(metadata);
			return ResponseEntity.status(HttpStatus.OK).body("{\"message\":\"OK\"}");
		} else {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readTree(response.body());
			JsonNode errorMessageNode = rootNode.path("error").path("message");

			String errorMessage = errorMessageNode.asText();
			System.out.println("Failed to send message: " + errorMessage);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\":\"" + errorMessage + "\"}");
		}
	}



	private static @NotNull Map<String, Object> getStringObjectMap(QueryDocumentSnapshot conversation)
	{
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
		return conversationMessages;
	}



}

