package aosPlanAutomator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kafka.utils.ShutdownableThread;

public class DataStreamConsumer extends ShutdownableThread {
    private final Logger log  = LoggerFactory.getLogger(DataStreamConsumer.class);
    private final KafkaConsumer consumer;
    private final List<String> topic = new CopyOnWriteArrayList<>();
    
    private int lineGenNum;

    private String clientConfigFile;
    public static final String KAFKA_SERVER_URL = "awaos-stg-kafka01.cloud.operative.com";
    public static final int KAFKA_SERVER_PORT = 9091;
    public static final String GROUP_ID = "SampleConsumer8404";

    public DataStreamConsumer() {
        super("SampleConsumerDemo", false);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER_URL+":"+KAFKA_SERVER_PORT);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        consumer = new KafkaConsumer(props);
        getTopics();
        System.out.println("Inside SampleConsumer Constructor");
    }

    private void getTopics(){
        try {
            Map map = consumer.listTopics(Duration.ofMillis(60000));
            topic.add("awaos_stg.datastream.digitalsandbox04_unifiedplanner.plan.header.output");
            consumer.subscribe(this.topic);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void doWork() {
        System.out.println("Polling for messages: " + this.topic);

        try {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));

            for (ConsumerRecord<String, String> record : records) {
                System.out.println("Received Message - Topic:[" + record.topic() + "], Value:[" + record.value() + "]");
                JSONObject messageJson = new JSONObject(record.value().toString());
                if (messageJson.getString("orderTypeId").equals("aQSgeSnyRWm9M_hzAe2_bA") &&
                		messageJson.getString("planStatus").equals("Internal Proposal Approvals") &&
                		!messageJson.getString("planStatusPrevious").equals(messageJson.getString("planStatus"))) {
                	    int planId = messageJson.getInt("planId");
                	    long startTime = System.currentTimeMillis();
                	    
                	    PlanGenerator planGenerator = new PlanGenerator(planId);
                	    planGenerator.generatePlan();
                	    
                	    String json = new String(Files.readAllBytes(Paths.get("autoGenConfig.json")));
                		JSONObject autoGenConfig = new JSONObject(json);
                	    if (autoGenConfig.getBoolean("triggerExport")) 
                	    	planGenerator.triggerExport();
                	                    	    
                	    long elapsedTime = ((System.currentTimeMillis() - startTime) / 1000) % 60;
                	    System.out.println("Workspace populated in " + elapsedTime + " seconds");
                	    WorkflowTask workflowTask = new WorkflowTask(planId);
                	    APICredentials apiCredentials = new APICredentials();
                	    workflowTask.getTaskId(apiCredentials);
                	    workflowTask.addComment(apiCredentials, "Workspace populated in " + elapsedTime + " seconds");
                	    workflowTask.approve(apiCredentials);
                	    
                	    if (autoGenConfig.getBoolean("calculateInventory")) 
                	    	planGenerator.calculateInventory();
                	    
                	    TimeUnit.SECONDS.sleep(autoGenConfig.getInt("delayBeforeSubmitSeconds"));
                	    if (autoGenConfig.getBoolean("submitToFinal")) 
                	    	planGenerator.submitToFinal();
                	    
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

	public String name() {
        return null;
    }

    public boolean isInterruptible() {
        return false;
    }
}
