package com.mixer.service;

import com.google.common.base.Strings;
import com.mixer.client.JobCoinClient;
import com.mixer.model.AddressTransaction;
import com.mixer.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class MixerService {

    private String houseAddress = "houseAddress";
    private SortedMap<LocalTime, List<Transaction>> map;
    private final JobCoinClient jobCoinClient;

    public MixerService(JobCoinClient jobCoinClient) {
        this.map = new TreeMap<>();
        this.jobCoinClient = jobCoinClient;
    }

    public void mixAndSendCoin(String source, String depositAddress, double coins, String... destination) {
        //postTransaction(transaction);
        depositAddress = validateAddress(depositAddress);
        List<String> finalAddress = new ArrayList<>();
        Arrays.stream(destination).forEach(x -> {
            if (!Strings.isNullOrEmpty(x.trim())) {
                finalAddress.add(x);
            }
        });
        double coinForEveryDestination = coins / destination.length;

        //Ideally it should be part of same contract in way it persisted so if
        // any of the transaction fails we can recover
        // from that point. Taking easy way out for purpose for this jobCoin
        //Post Transaction from source to depositAddress
        log.info("Transferring from source ={} to deposit={} ", source, depositAddress);
        //Technically we should check if the source address has sufficient coins using address endpoint,
        // but since post returns insufficient fund HttpException if coins not available skipping that check for this exercise
        // let exception handling take care of that scenario
        postTransaction(new Transaction(source, depositAddress, String.valueOf(coins)));
        //Post Transaction from deposit to HouseAddress
        log.info("Transferring from deposit={} to houseAddress={}", depositAddress, houseAddress);
        postTransaction(new Transaction(depositAddress, houseAddress, String.valueOf(coins)));
        //Scheduling
        scheduleFromHouseDestination(finalAddress, houseAddress, coinForEveryDestination);
        sendCoins();


        //Verify all the destination address has more than equal to
        //finalAddress.forEach(x -> jobCoinClient.getAddressTransaction(x));;
        //Verify all the transaction have been posted
        //jobCoinClient.getTransactions();

    }

    private void scheduleFromHouseDestination(List<String> finalAddress,
                                              String houseAddress,
                                              double coinForEveryDestination) {

        LocalTime time = LocalTime.now().plus(30, ChronoUnit.SECONDS);
        for (String d : finalAddress) {
            List<Transaction> transactions = map.getOrDefault(time, new ArrayList<>());
            Transaction transaction = new Transaction(houseAddress,
                    d,
                    String.valueOf(coinForEveryDestination));
            transactions.add(transaction);
            map.put(time, transactions);
            time = time.plus(30, ChronoUnit.SECONDS);
        }
    }

    private String validateAddress(String address) {
        if (Strings.isNullOrEmpty(address.trim()))
            return UUID.randomUUID().toString();

        return address;
    }

    @Scheduled(fixedRate = 3000)
    public void sendCoins() {
        LocalTime currentTime = LocalTime.now();
        while (true) {
            if (map == null || map.size() == 0)
                break;
            LocalTime time = map.firstKey();
            if (currentTime.compareTo(time) >= 0) {
                List<Transaction> transactions = map.get(time);
                transactions.forEach(this::postTransaction);
                map.remove(time);
                log.info("Posted transaction for time={}", time);
            } else {
                break;
            }
        }
    }

    /**
     * Default retry = 3
     * Adding retry to cover if rest endpoint is not available for whatever reason
     * Ideally when errored out the transaction should go into the ledger so that coin is not lost
     * There should be better error handling, in-way the end user is not losing the coin
     *
     * @param transaction
     */
    @Retryable(backoff = @Backoff(delay = 1000), maxAttempts = 4)
    private void postTransaction(Transaction transaction) {
        jobCoinClient.postTransaction(transaction);
    }


}
