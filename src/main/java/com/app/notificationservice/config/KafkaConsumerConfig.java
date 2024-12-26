package com.app.notificationservice.config;

import com.app.notificationservice.dtos.SendEmailRequestDto;
import com.app.notificationservice.util.EmailUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {

    private ObjectMapper objectMapper;


    public KafkaConsumerConfig(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }
    // if there are multiple users running we end in listening to the same message
    // if we dont provide group id
    // we need to make sure only one consumer from the consumer group should
    // listen to which will be taken care by kafka listener

    @KafkaListener(topics = "sendEmail", groupId = "notificationservice")
    public void sendEmailEvent(String message) throws JsonProcessingException {
        SendEmailRequestDto sendEmailRequestDto = objectMapper.readValue(message, SendEmailRequestDto.class);
        System.out.println("Got the message");


        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                // generate the app spefici password for your account and provide it here
                return new PasswordAuthentication("scaler.xxx@gmail.com", "");
            }
        };
        Session session = Session.getInstance(props, auth);

        EmailUtil.sendEmail(session, sendEmailRequestDto.getTo(), sendEmailRequestDto.getSubject(), sendEmailRequestDto.getBody());
    }
}
