package com.taotao.rest.rockeyMQ;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;  
import com.alibaba.rocketmq.client.producer.SendResult;  
import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;  
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;  
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;  
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;  
import com.alibaba.rocketmq.common.consumer.ConsumeFromWhere;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt; 
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;  


public class RockeyMQTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RockeyMQTest.class);
	
	@Test
	public void testProducer() {
		DefaultMQProducer producer = new DefaultMQProducer("Producer");  
        //nameserver服务,多个以;分开   
        producer.setNamesrvAddr("192.168.18.149:9876;192.168.18.149:9876");  
        try{  
            producer.start();  
            for(Integer i=0;i<10000;i++){
            	Message msg = new Message("PushTopic"//topic
            			,"push"//tags
            			,("Just for test.").getBytes());  
                SendResult result = producer.send(msg);  
//                System.out.println("id:"+result.getMsgId()+" result:" +result.getSendStatus());
                System.out.println(result);
            	
            }
          
        }catch(Exception e){  
            e.printStackTrace();  
        }finally {  
            producer.shutdown();  
        }  
	}
	
	
	@Test
	public void testConsumer() {
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("PushConsumer");    
        consumer.setNamesrvAddr("192.168.18.148:9876;192.168.18.149:9876");     
        try {    
            //订阅PushTopic下Tag为push的消息    
            consumer.subscribe("PushTopic", "push");    
            //程序第一次启动从消息队列头取数据    
            consumer.setConsumeFromWhere(    
                    ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);    
            consumer.registerMessageListener(    
                new MessageListenerConcurrently() {    
                    public ConsumeConcurrentlyStatus consumeMessage(    
                            List<MessageExt> list,    
                            ConsumeConcurrentlyContext Context) {    
                        Message msg = list.get(0);    
                        System.out.println("取出的Message："+msg.toString());    
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;    
                    }    
                }    
            );    
            consumer.start();    
        } catch (Exception e) {    
            e.printStackTrace();    
        }    
	}
	
	
}
