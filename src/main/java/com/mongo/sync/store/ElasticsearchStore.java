package com.mongo.sync.store;

import org.frameworkset.elasticsearch.boot.BBossESStarter;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ElasticsearchStore {

    private final Logger logger = LoggerFactory.getLogger(ElasticsearchStore.class);

    @Autowired
    private BBossESStarter bbossESStarter;

    public void addDocument(String index,Map<String,Object> map){
        ClientInterface clientUtil = bbossESStarter.getRestClient();
        String response = clientUtil.addDocument(index,map);
        logger.info("es add document response:{}",response);
        if(logger.isDebugEnabled()){
            logger.debug("es add document response:{}",response);
        }
    }

    public void deleteDocument(String index,String id){
        ClientInterface clientUtil = bbossESStarter.getRestClient();
        String response = clientUtil.deleteDocument(index,null,id);
        logger.info("es delete document response:{}",response);
        if(logger.isDebugEnabled()){
            logger.debug("es delete document response:{}",response);
        }
    }
    public Map<String,Object> getDocument(String index,String id){
        ClientInterface clientUtil = bbossESStarter.getRestClient();
        return clientUtil.getDocument(index,id, Map.class);
    }
}
