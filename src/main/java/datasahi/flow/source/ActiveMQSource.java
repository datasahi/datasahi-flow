package datasahi.flow.source;

import datasahi.flow.ds.DataServerType;
import datasahi.flow.health.VerifyRequest;
import datasahi.flow.health.VerifyResponse;
import datasahi.flow.sync.DataPipe;
import datasahi.flow.sync.DataRecord;
import datasahi.flow.sync.Dataset;
import datasahi.flow.sync.Flow;
import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActiveMQSource implements DataSource, Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQSource.class);

    private final String brokerId;
    private final String brokerUrl;
    private final String username;
    private final String password;
    private final String destinationName;
    private final boolean isPubSub;

    private final List<DataPipe> dataPipes = new ArrayList<>();
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile boolean verifyMode = false;
    private VerifyResponse verifyResponse = null;

    public ActiveMQSource(String brokerId, String brokerUrl, String username,
                          String password, String destinationName, boolean isPubSub) {
        this.brokerId = brokerId;
        this.brokerUrl = brokerUrl;
        this.username = username;
        this.password = password;
        this.destinationName = destinationName;
        this.isPubSub = isPubSub;
    }

    @Override
    public String getId() {
        return brokerId;
    }

    @Override
    public DataServerType getType() {
        // You might want to create an enum or define this based on your requirements
        return DataServerType.ACTIVEMQ;
    }

    @Override
    public void addDataPipe(DataPipe dataPipe) {
        dataPipes.add(dataPipe);
    }

    @Override
    public List<DataPipe> getDataPipes() {
        return new ArrayList<>(dataPipes);
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            Thread thread = new Thread(this);
            thread.setName("ActiveMQSource-" + brokerId);
            thread.start();
        }
    }

    @Override
    public void stop() {
        running.set(false);
        try {
            if (consumer != null) consumer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        } catch (JMSException e) {
            LOG.error("Error stopping ActiveMQ connection", e);
        }
    }

    @Override
    public void run() {
        try {
            // Create connection factory
            ActiveMQConnectionFactory connectionFactory =
                    new ActiveMQConnectionFactory(username, password, brokerUrl);

            connection = connectionFactory.createConnection();
            connection.start();

            // Create session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create destination
            Destination destination = isPubSub
                    ? session.createTopic(destinationName)
                    : session.createQueue(destinationName);

            // Create consumer
            consumer = session.createConsumer(destination);

            // Message listener
            consumer.setMessageListener(message -> {
                try {
                    processMessage(message);
                } catch (JMSException e) {
                    LOG.error("Error processing message", e);
                }
            });

            LOG.info("Started ActiveMQ source: {}", brokerId);

            // Keep thread alive while running
            while (running.get()) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            LOG.error("Error in ActiveMQ source", e);
            running.set(false);
        } finally {
            stop();
        }
    }

    private void processMessage(Message message) throws JMSException {
        // Convert message to a format suitable for DataRecord
        Map<String, String> messageData = extractMessageData(message);

        DataRecord<Map<String, String>> record =
                new DataRecord<>(brokerId, java.time.LocalDateTime.now(),
                        getMessageKey(message), messageData);

        for (DataPipe pipe : dataPipes) {
            Flow flow = pipe.getFlow();
            Dataset sourceDataset = flow.getSourceDataset();

            // Add your filtering logic similar to RedisSource
            boolean matched = matchDataset(sourceDataset, messageData);

            if (matched) {
                pipe.addRecord(record);

                if (verifyMode) {
                    verifyResponse.incrementMatched();
                }
            } else if (verifyMode) {
                verifyResponse.incrementUnmatched();
            }
        }
    }

    private Map<String, String> extractMessageData(Message message) throws JMSException {
        Map<String, String> data = new HashMap<>();

        if (message instanceof TextMessage) {
            data.put("text", ((TextMessage) message).getText());
        } else if (message instanceof MapMessage) {
            MapMessage mapMessage = (MapMessage) message;
            @SuppressWarnings("unchecked")
            Enumeration<String> mapNames = mapMessage.getMapNames();
            while (mapNames.hasMoreElements()) {
                String name = mapNames.nextElement();
                data.put(name, mapMessage.getString(name));
            }
        }

        // Add message properties
        @SuppressWarnings("unchecked")
        Enumeration<String> propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String name = propertyNames.nextElement();
            data.put("prop_" + name, message.getStringProperty(name));
        }

        return data;
    }

    private String getMessageKey(Message message) throws JMSException {
        // Generate a unique key for the message
        return message.getJMSMessageID();
    }

    private boolean matchDataset(Dataset dataset, Map<String, String> messageData) {
        // Implement dataset matching logic similar to RedisSource
        // This is a placeholder and should be adapted to your specific requirements
        if (dataset.getType().equals("map")) {
            // Add specific filtering logic
            return true;
        }
        return false;
    }

    @Override
    public VerifyResponse verify(VerifyRequest request) {
        this.verifyMode = true;
        this.verifyResponse = new VerifyResponse(request);
        long start = System.currentTimeMillis();

        start();

        try {
            // Wait for verification to complete
            while (!verifyResponse.isReady()) {
                Thread.sleep(100);
                verifyResponse.setDurationMilis((int) (System.currentTimeMillis() - start));
            }
        } catch (InterruptedException e) {
            LOG.warn("Verification interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            stop();
            this.verifyMode = false;
        }

        return verifyResponse;
    }
}