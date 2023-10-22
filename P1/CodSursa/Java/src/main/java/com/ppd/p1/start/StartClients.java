package com.ppd.p1.start;

import com.ppd.p1.configuration.ClientConfiguration;
import com.ppd.p1.networking.Client;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.ppd.p1.commons.Constants.*;


public class StartClients {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(ClientConfiguration.class);
        Thread[] clientThreads = new Thread[NR_CLIENTS];
        for (int i = 0; i < NR_CLIENTS; i++) {
            int finalI = i;
            clientThreads[i] = new Thread(() -> (new Client(SERVER_HOST, SERVER_PORT, finalI)).run());
            clientThreads[i].start();
        }
        for (int i = 0; i < NR_CLIENTS; i++) {
            try {
                clientThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
