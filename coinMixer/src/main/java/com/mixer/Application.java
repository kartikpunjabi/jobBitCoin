package com.mixer;

import com.mixer.client.JobCoinClient;
import com.mixer.service.MixerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

/**
 * Free BitCoin Mixer, no fee charge ever
 */

@SpringBootApplication
@EnableScheduling
@EnableRetry
@Slf4j
public class Application {


    private final MixerService mixerService;

    public Application(JobCoinClient client, MixerService mixerService) {
        this.mixerService = mixerService;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class);
        Application application = context.getBean(Application.class);
        application.start();
    }

    private void start() {
        //while (true) {
            System.out.println("Starting Job coin mixer \n");
            Scanner consoleIn = new Scanner(System.in);

            System.out.println("Enter source address: ");
            String sourceAddress = consoleIn.next();

            System.out.println("Enter deposit address");
            String depositAddress = consoleIn.next();

            System.out.println("Enter destination address, if multiple address delimit it by comma: ");
            String[] destinationAddress = consoleIn.next().split(",");

            //Should have better handling, if values are not double
            System.out.println("Enter number of coins to transfer: ");
            double coins = consoleIn.nextDouble();

            try {
                mixerService.mixAndSendCoin(sourceAddress, depositAddress, coins, destinationAddress);
                log.info("Transaction was successful. Ready for new transaction");
            } catch (Exception ex) {
                log.error("Error while posting transaction, please try again. Exception: {}", ex.getMessage());
            }
       // }


    }
}
