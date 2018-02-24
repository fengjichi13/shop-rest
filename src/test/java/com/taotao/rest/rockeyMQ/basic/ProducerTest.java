package com.taotao.rest.rockeyMQ.basic;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;

public class ProducerTest {

	public static void main(String[] args) {
		DefaultMQProducer producer = new DefaultMQProducer("Producer");  
        //nameserver服务,多个以;分开   
        producer.setNamesrvAddr("192.168.18.149:9876;192.168.18.149:9876");  
        
//        producer.setRetryTimesWhenSendFailed(10);//指定失败重发次数
        try{  
            producer.start();  
            for(Integer i=0;i<1000;i++){
            	Message msg = new Message("PushTopic"//topic
            			,"push"//tags
            			,("Just for test.").getBytes());  
                SendResult result = producer.send(msg);
//                SendResult result = producer.send(msg,1000);//指定发送超过多长时间算失败
//                System.out.println("id:"+result.getMsgId()+" result:" +result.getSendStatus());
                System.out.println(result);
            	
            }
          
        }catch(Exception e){  
            e.printStackTrace();  
        }finally {  
            producer.shutdown();  
        }  

	}

}
