package io.exercise.api.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 *
 */
public interface IMongoDB {
	public MongoDatabase getMongoDatabase();
	public MongoClient getMongoClient();
}
