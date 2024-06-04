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
import java.util.logging.Logger;

@Service
public class FirestoreService
{
	private final Firestore db;

	public FirestoreService()
	{
		db = FirestoreClient.getFirestore();
	}

	public void putReceivedMessage(String senderId, String messageText, long timestamp, String mid) throws ExecutionException, InterruptedException
	{
		// Create the message data
		Map<String, Object> messageData = new HashMap<>();
		messageData.put("messageText", messageText);
		messageData.put("timestamp", timestamp);
		messageData.put("mid", mid);

		// Check if the senderId document exists
		DocumentSnapshot senderDoc = db.collection("message").document(senderId).get().get();
		long count;
		if (senderDoc.exists())
		{
			// If the document exists, get the conversationCount and increment it
			Long conversationCount = senderDoc.getLong("conversationCount");
			//count = conversationCount != null ? conversationCount + 1 : 1;
			count = conversationCount;
		} else
		{
			// If the document does not exist, set count to 1
			count = 1;
		}
		DocumentSnapshot conversationMetadataSnapshot = db.collection("message").document(senderId).collection(String.valueOf(count)).document("conversation_metadata").get().get();
		Long lastMessageTimestamp = conversationMetadataSnapshot.getLong("lastMessageTimestamp");
		if (lastMessageTimestamp != null && (timestamp - lastMessageTimestamp) > 60 * 60 * 1000) // New conversation if the last message was sent more than an hour ago
		{
			count += 1;
			senderDoc.getReference().update("conversationCount", count);
			Logger.getGlobal().info("New conversation started");
		}

		// Save the message data to Firestore
		db.collection("message").document(senderId).collection(String.valueOf(count)).document(mid).set(messageData);

		// Update the conversationCount in the senderId document
		Map<String, Object> senderData = new HashMap<>();
		senderData.put("conversationCount", count);
		db.collection("message").document(senderId).set(senderData);



//		// Get the metadata document
		DocumentSnapshot metadataDoc = db.collection("message").document(senderId).collection(String.valueOf(count)).document("conversation_metadata").get().get();
//
		// Create or update the metadata
		Map<String, Object> metadata = new HashMap<>();
		if (metadataDoc.exists())
		{
			metadata = metadataDoc.getData();
		} else
		{
			metadata.put("firstMessageTimestamp", timestamp);
		}
		metadata.put("lastMessage", messageText);
		metadata.put("lastMessageTimestamp", timestamp);
		metadata.put("advisorId", null);

		// Save the metadata to Firestore
		db.collection("message").document(senderId).collection(String.valueOf(count)).document("conversation_metadata").set(metadata);

	}

public void putAdvisor(String advisorId, String name, String status) throws ExecutionException, InterruptedException {
    // Create the advisor data
    Map<String, Object> advisorData = new HashMap<>();
    advisorData.put("name", name);
    advisorData.put("status", status);

    // Save the advisor data to Firestore
    db.collection("advisors").document(advisorId).set(advisorData);
}


	// public List<DocumentSnapshot> getMessageHistory(String userId) throws ExecutionException, InterruptedException {
	// 	List<QueryDocumentSnapshot> queryDocumentSnapshots = 		db.collection("message").document("25240652615526181").collection(String.valueOf(1)).document("1").zorderBy("timestamp").get().get().getDocuments();
	// 	List<DocumentSnapshot> documentSnapshots = new ArrayList<>(queryDocumentSnapshots);
	// 	return documentSnapshots;
	// }

	public DocumentSnapshot getLastMessage(String userId) throws ExecutionException, InterruptedException {
		return db.collection(userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get().get().getDocuments().get(0);
	}
}
