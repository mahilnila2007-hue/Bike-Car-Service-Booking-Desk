package com.garage.management.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Generic Firestore CRUD repository providing base operations for all collections.
 */
@Component
public class FirestoreRepository {

    private static final Logger log = LoggerFactory.getLogger(FirestoreRepository.class);

    private final Firestore firestore;

    public FirestoreRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Save or overwrite a document in a collection.
     */
    public void save(String collection, String documentId, Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        firestore.collection(collection).document(documentId).set(data).get();
        log.debug("Saved document {} in collection {}", documentId, collection);
    }

    /**
     * Update specific fields of a document (merge).
     */
    public void update(String collection, String documentId, Map<String, Object> data)
            throws ExecutionException, InterruptedException {
        firestore.collection(collection).document(documentId).update(data).get();
    }

    /**
     * Get a document by ID.
     */
    public Optional<Map<String, Object>> findById(String collection, String documentId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = firestore.collection(collection).document(documentId).get().get();
        if (snapshot.exists()) {
            Map<String, Object> data = snapshot.getData();
            if (data != null) {
                data.put("id", snapshot.getId());
            }
            return Optional.ofNullable(data);
        }
        return Optional.empty();
    }

    /**
     * Get all documents in a collection.
     */
    public List<Map<String, Object>> findAll(String collection)
            throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(collection).get().get();
        List<Map<String, Object>> results = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data != null) {
                data.put("id", doc.getId());
                results.add(data);
            }
        }
        return results;
    }

    /**
     * Query documents where a field equals a value.
     */
    public List<Map<String, Object>> findByField(String collection, String field, Object value)
            throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(collection)
                .whereEqualTo(field, value).get().get();
        List<Map<String, Object>> results = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data != null) {
                data.put("id", doc.getId());
                results.add(data);
            }
        }
        return results;
    }

    /**
     * Query documents where a field equals any of the given values.
     */
    public List<Map<String, Object>> findByFieldIn(String collection, String field, List<Object> values)
            throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(collection)
                .whereIn(field, values).get().get();
        List<Map<String, Object>> results = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data != null) {
                data.put("id", doc.getId());
                results.add(data);
            }
        }
        return results;
    }

    /**
     * Query with two equality conditions.
     */
    public List<Map<String, Object>> findByTwoFields(String collection,
                                                      String field1, Object value1,
                                                      String field2, Object value2)
            throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(collection)
                .whereEqualTo(field1, value1)
                .whereEqualTo(field2, value2)
                .get().get();
        List<Map<String, Object>> results = new ArrayList<>();
        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data != null) {
                data.put("id", doc.getId());
                results.add(data);
            }
        }
        return results;
    }

    /**
     * Delete a document by ID.
     */
    public void delete(String collection, String documentId)
            throws ExecutionException, InterruptedException {
        firestore.collection(collection).document(documentId).delete().get();
        log.debug("Deleted document {} from collection {}", documentId, collection);
    }

    /**
     * Check if a document with a given field value exists.
     */
    public boolean existsByField(String collection, String field, Object value)
            throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(collection)
                .whereEqualTo(field, value).limit(1).get().get();
        return !querySnapshot.isEmpty();
    }

    /**
     * Run a Firestore transaction with callback.
     */
    public <T> T runTransaction(Transaction.Function<T> updateFunction)
            throws ExecutionException, InterruptedException {
        ApiFuture<T> future = firestore.runTransaction(updateFunction);
        return future.get();
    }

    /**
     * Get the Firestore instance for advanced queries.
     */
    public Firestore getFirestore() {
        return firestore;
    }

    /**
     * Get a CollectionReference for a given collection name.
     */
    public CollectionReference collection(String name) {
        return firestore.collection(name);
    }

    /**
     * Get a DocumentReference for a specific document.
     */
    public DocumentReference document(String collection, String documentId) {
        return firestore.collection(collection).document(documentId);
    }

    /**
     * Generate a new document ID for a collection.
     */
    public String generateId(String collection) {
        return firestore.collection(collection).document().getId();
    }

    /**
     * Count documents matching a field value.
     */
    public long countByField(String collection, String field, Object value)
            throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = firestore.collection(collection)
                .whereEqualTo(field, value).get().get();
        return querySnapshot.size();
    }
}
