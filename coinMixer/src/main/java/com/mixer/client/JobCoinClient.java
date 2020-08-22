package com.mixer.client;

import com.mixer.model.AddressTransaction;
import com.mixer.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
/*
 * Overall there are missing error handling pieces in JobCoinClient
 */
public class JobCoinClient {

    private final RestTemplate restTemplate;
    private final String apiTransactionURL;
    private final String apiAddressURL;

    public JobCoinClient(RestTemplate restTemplate,
                         @Value("${spring.url.apiTransactionsUrl}")
                                 String apiTransactionURL,
                         @Value("${spring.url.apiAddressesUrl}")
                                 String apiAddressURL) {
        this.restTemplate = restTemplate;
        this.apiTransactionURL = apiTransactionURL;
        this.apiAddressURL = apiAddressURL;
    }

    public List<Transaction> getTransactions() {
        ResponseEntity<List<Transaction>> transactionList = restTemplate
                .exchange(apiTransactionURL,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Transaction>>() {
                        });
        return transactionList.getBody();
    }

    public void postTransaction(Transaction transaction) {
        HttpEntity<Transaction> request = new HttpEntity<>(transaction);
        try {
            restTemplate
                    .postForEntity(apiTransactionURL, request, String.class);
            //Just coding for 4X error while posting transaction
        } catch (HttpClientErrorException ex) {
            log.error("Exception, insufficient fund in source account={}", transaction.getFromAddress());
            throw new RuntimeException(ex.getMessage());
        }

        log.info("Transaction successfully posted sourceAdd={}, toAddress={}, amount={}",
                transaction.getFromAddress(),
                transaction.getToAddress(),
                transaction.getAmount());
    }

    public AddressTransaction getAddressTransaction(String address) {
        String add = apiAddressURL + "/" + address;
        ResponseEntity<AddressTransaction> addressTransaction =
                restTemplate.exchange(add,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<AddressTransaction>() {
                        });

        return addressTransaction.getBody();
    }


}
