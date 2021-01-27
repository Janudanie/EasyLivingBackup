package dk.easyliving.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import dk.easyliving.dto.units.LdrSensor;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


import com.rabbitmq.client.AMQP.BasicProperties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
// This app should do the following
// get up

@EnableMongoRepositories
@SpringBootApplication
public class BackupApplication  implements CommandLineRunner {

    private static Connection connection;
    private static Channel channel;
    //private final static String QUEUE_NAME = "AddController";

    public BackupApplication()throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.1.240");

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    @Autowired
    private LdrRepository ldrRepository;
    private PirRepository pirRepository;
    private RelayRepository relayRepository;
    public static void main(String[] args) throws Exception {
        SpringApplication.run(BackupApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        subcribeToQueues();
    }

    private void subcribeToQueues(){
        subScripeToAddLdrSensor();
        subScripeToRemoveLdrSensor();
        subScribeToGetAllLdrSensor();
    }

    private void subScripeToAddLdrSensor()  {
        String QUEUE_NAME = "AddLdrSensor";
        try {
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            DeliverCallback callBackAddLdrSensor = (consumerTag, delivery) ->
            {
                String message = new String(delivery.getBody(), "UTF-8");
                ObjectMapper obj = new ObjectMapper();
                LdrSensor temp = obj.readValue(message, LdrSensor.class);
                String result = "";
                if(!ldrRepository.existsByName(temp.getName()) &&
                        !ldrRepository.existsByMacAdd(temp.getMacAdd()) ) {
                        ldrRepository.save(temp);
                        System.out.println("Added Ldr sensor : " + temp.getName());
                        result += "Ldr sensor added";
                    }
                else {
                    System.out.println("Cannot add the Ldr sensor, it already exists");
                    result += "Ldr sensor already existed";
                }
                BasicProperties replyProps = new BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();
                    try{


                    }
                    catch(Exception e){
                        System.out.println("Error :" + e.getMessage());
                    }
                    finally {
                        System.out.println("Sending back : " + result);
                        channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, result.getBytes("UTF-8"));
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    }

            };
            channel.basicConsume(QUEUE_NAME, false, callBackAddLdrSensor, consumerTag -> { });
            System.out.println("Listning to : " + QUEUE_NAME);
         }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("Error happened");
        }
   }

    private void subScripeToRemoveLdrSensor(){
        String QUEUE_NAME = "RemoveLdrSensor";

        try{
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            System.out.println("Listning to : " + QUEUE_NAME);

            DeliverCallback callBackRemoveLdrSensor = (consumerTag,delivery) ->
            {
                System.out.println("Deleting an Ldr sensor");
                String result;
                String message = new String(delivery.getBody(), "UTF-8");
                if  (!ldrRepository.existsByMacAdd(message)){
                    result = "Ldr does not exist";
                }
                else{
                    ldrRepository.deleteByMacAdd(message);
                    result = "Ldr sensor is deleted";
                }

                BasicProperties replyProps = new BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();
                try{


                }
                catch(Exception e){
                    System.out.println("Error :" + e.getMessage());
                }
                finally {
                    System.out.println("Sending back : " + result);
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, result.getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }


            };
            channel.basicConsume(QUEUE_NAME,false,callBackRemoveLdrSensor, consumerTag ->{});
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("Error happened");
        }
    }

    private void subScribeToGetAllLdrSensor(){
        System.out.println("getting all LDR");
        String QUEUE_NAME = "GetAllLdr";
        try {
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            DeliverCallback callBackgetAllLdrSensor = (consumerTag, delivery) ->
            {
                List<LdrSensor> tempLdrList = ldrRepository.findAll();
                ObjectMapper obj = new ObjectMapper();
                String temp;

                final ByteArrayOutputStream out = new ByteArrayOutputStream();

                obj.writeValue(out,tempLdrList);

                String tempJson = out.toString();
                BasicProperties replyProps = new BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();
                try{
                }
                catch(Exception e){
                    System.out.println("Error :" + e.getMessage());
                }
                finally {
                    System.out.println("Sending back : ");
                    channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, tempJson.getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            channel.basicConsume(QUEUE_NAME, false, callBackgetAllLdrSensor, consumerTag -> { });
            System.out.println("Listning to : " + QUEUE_NAME);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("Error happened");
        }
    }

    public void sendMessage(String exchange, String topic, String message){
        try
        {
            channel.basicPublish(exchange, topic, null, message.getBytes("UTF-8"));
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.out.println("Was an error");
        }
    }
}