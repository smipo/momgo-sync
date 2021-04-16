package com.mongo.sync.source;

import com.mongo.sync.sink.MongoSink;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;


@Component
public class MongoOplogSource implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(MongoOplogSource.class);

    private MongoClient mongoClient = null;

    @Value("${spring.data.mongodb.uri}")
    private String mongodbUri;

    @Value("${mongodb.tables}")
    private String mongodbTables;

    @Autowired
    private MongoSink mongoSink;

    private AtomicBoolean running = new AtomicBoolean(true);

    @Override
    public void afterPropertiesSet() throws Exception {
        mongoClient = MongoClients.create(mongodbUri);
        mongoSink.afterPropertiesSet();
        new Thread(() -> {
            run();
        }).start();
    }

    @Override
    public void destroy() throws Exception {
        logger.info("mongo sync elasticsearch end ...");
        running.set(false);
        if(mongoClient != null){
            mongoClient.close();
        }
    }

    public void run() {
        logger.info("mongo sync elasticsearch start ...");
        final MongoCollection<Document> collection = mongoClient.getDatabase("local").getCollection("oplog.rs");
        while (running.get()) {
            MongoCursor<Document> cursor = collection.find(mongoSink.getLastTimeStamp())
                    .sort(new BasicDBObject("$natural", 1))
                    .limit(1000)
                    .iterator();
            if (cursor != null && cursor.hasNext()){
                final Document document = cursor.next();
                if(logger.isDebugEnabled()){
                    logger.debug("document structure:{}",document.toJson());
                }
                final String ns = (String) document.get("ns");
                if (StringUtils.isEmpty(ns)){
                    mongoSink.setLastTimeStamp(document);
                    continue;
                }
                final String mapping = ns.split("\\.")[1];
                if (StringUtils.isEmpty(mapping) || !mongodbTables.contains(mapping)) {
                    mongoSink.setLastTimeStamp(document);
                    continue;
                }
                mongoSink.send(document);
                mongoSink.setLastTimeStamp(document);
            }
        }
    }
}