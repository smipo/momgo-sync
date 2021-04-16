package com.mongo.sync.sink;

import com.mongo.sync.store.ElasticsearchStore;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.bson.types.BSONTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class MongoSink {

    private final Logger logger = LoggerFactory.getLogger(MongoSink.class);

    private BSONTimestamp lastTimeStamp = null;

    @Autowired
    private ElasticsearchStore elasticsearchStore;

    @Resource
    private RedisTemplate<String, LastTime> redisTemplate;

    public void afterPropertiesSet(){
        LastTime lastTime = redisTemplate.opsForValue().get("ts");
        if(lastTime != null){
            lastTimeStamp = new BSONTimestamp(lastTime.getTime(), lastTime.getInc());
        }
    }

    public void send(Document document){
        String operation = document.getString("op");
        Document o = (Document) document.get("o");
        String ns = (String) document.get("ns");
        String[] arr = ns.split("\\.");
        String index = String.format("%s_%s", arr[0].toLowerCase(),  arr[1].toLowerCase());
        o.remove("_class");
        switch (operation) {
            case "u":
                Document o2 = (Document) document.get("o2");
                String _id = o2.get("_id").toString();
                Document updateSet = (Document)o.get("$set");
                Map<String,Object> m = elasticsearchStore.getDocument(index,_id);
                if (m == null){
                    logger.warn("not fund index = {},  id = {} update error", index, _id);
                    return ;
                }
                updateSet.putAll(m);
                elasticsearchStore.addDocument(index,updateSet);
                break;
            case "i":
                elasticsearchStore.addDocument(index,o);
                break;
            case "d":
                elasticsearchStore.deleteDocument(index,o.get("_id").toString());
                break;
        }
    }
    public BasicDBObject getLastTimeStamp() {
        final BasicDBObject timeQuery = new BasicDBObject();
        if (lastTimeStamp != null) {
            timeQuery.put("ts", BasicDBObjectBuilder.start("$gt", lastTimeStamp).get());
        }
        return timeQuery;
    }
    public void setLastTimeStamp(Document document){
        BsonTimestamp ts = (BsonTimestamp) document.get("ts");
        lastTimeStamp = new BSONTimestamp(ts.getTime(), ts.getInc());
        redisTemplate.opsForValue().set("ts",new LastTime(ts.getTime(), ts.getInc()));
    }
}
