package com.mcd;

import com.mcd.model.Event;
import com.mcd.service.KafkaConsumerService;
import com.mcd.service.XmlProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DirtiesContext
public class KafkaConsumerServiceTest {

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @MockBean
    private XmlProcessingService xmlProcessingService;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    public void testListen() {
        // Mock input data
        String key = "123";
        String value = "<Event RegId=\"987\" Type=\"Ev_Custom\" Time=\"20240601121426\">\n" +
                "    <Ev_Custom>\n" +
                "        <Info code=\"0000\" data=\"test data\" />\n" +
                "    </Ev_Custom>\n" +
                "</Event>";

        // Mocking XML processing service
        Event event = new Event();
        event.setRegId("987");
        event.setType("Ev_Custom");
        event.setTime("20240601121426");
        // Add any other necessary configurations to the event object
        // Mock the behavior of the XML processing service
        // when it parses the XML event
        // You can also mock the behavior of the processEvent method if necessary

        // Mocking Kafka template behavior
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> valueCaptor = ArgumentCaptor.forClass(Object.class);

        // Call the method to be tested
        kafkaConsumerService.listen(new ConsumerRecord<>("topic", 0, 0, key, value));

        // Verify if the necessary methods were called with the correct arguments
        verify(xmlProcessingService).parseXmlEvent(value);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        // Assert the captured values
        assertEquals("${spring.kafka.topic.output}", topicCaptor.getValue());
        assertEquals("123-987", keyCaptor.getValue()); // Make sure key is constructed as expected
        // You might need to do some additional assertions depending on the behavior of your code
    }
}
