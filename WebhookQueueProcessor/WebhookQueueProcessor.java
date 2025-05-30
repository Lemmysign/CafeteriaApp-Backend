package Evercare_CafeteriaApp.WebhookQueueProcessor;

import Evercare_CafeteriaApp.DTO.PaymentDtoPackage.PaystackDTO;
import Evercare_CafeteriaApp.Services.PaystackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class WebhookQueueProcessor {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final PaystackService paystackService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookQueueProcessor(PaystackService paystackService) {
        this.paystackService = paystackService;
        startWorker();
    }

    public void enqueue(String rawBody) {
        queue.offer(rawBody);
    }

    private void startWorker() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    String rawBody = queue.take(); // Blocks until something is added
                    PaystackDTO.WebhookEventData webhookEvent =
                            objectMapper.readValue(rawBody, PaystackDTO.WebhookEventData.class);

                    boolean processed = paystackService.processWebhookEvent(webhookEvent);

                    if (!processed) {
                        // Optional: Retry or log
                        System.err.println("Failed to process webhook for reference: "
                                + webhookEvent.getData().getReference());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // Optional: Persist failed raw JSON for later analysis
                }
            }
        });
        worker.setDaemon(true); // Allows JVM to exit if only this thread is running
        worker.start();
    }
}
