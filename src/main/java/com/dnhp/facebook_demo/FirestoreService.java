package com.dnhp.facebook_demo;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {
	private final Firestore db;
	public FirestoreService() {
		db = FirestoreClient.getFirestore();
	}

	public void sendMessage(String userId, String messageText, String senderId) {
		Map<String, Object> data = new HashMap<>();
		data.put("messageText", messageText);
		data.put("timestamp", FieldValue.serverTimestamp());
		data.put("senderId", senderId);

		db.collection(userId).document().set(data);
	}

	public void sendMessageToFacebook(String recipientId, String messageText) {
		String url = "https://graph.facebook.com/v18.0/me/messages?access_token=EABsLirhuG9kBO1kHgWn5AecLhNPksgVKogL72F4oB8sCZB9roIZC02Uxv4IngGG0SZCJzseTeBwaJSyKK43ZAkZC5oR3Tg3iu3VSJGxl1c3VhFAFbIrBzWi1Cqt4gljsbIPJpxyXJsXKGw1QIVNgunF2d755bOXyqQ9FjZA17dyb5yUZC1eusvn7RL2orzrbT4ZD";

		Map<String, Object> message = new HashMap<>();
		message.put("text", messageText);

		Map<String, Object> recipient = new HashMap<>();
		recipient.put("id", recipientId);

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("message", message);
		requestBody.put("messaging_type", "RESPONSE");
		requestBody.put("recipient", recipient);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.postForObject(url, request, String.class);
	}

	public List<DocumentSnapshot> getMessageHistory(String userId) throws ExecutionException, InterruptedException {
		List<QueryDocumentSnapshot> queryDocumentSnapshots = db.collection(userId).orderBy("timestamp").get().get().getDocuments();
		List<DocumentSnapshot> documentSnapshots = new ArrayList<>(queryDocumentSnapshots);
		return documentSnapshots;
	}

	public DocumentSnapshot getLastMessage(String userId) throws ExecutionException, InterruptedException {
		return db.collection(userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get().get().getDocuments().get(0);
	}
}
