package ru.yandex.practicum.exchange.generator.service.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Currency;

@Service
public class CurrencyRateProducer {

    private final KafkaTemplate<String, Currency> kafkaTemplate;

    public CurrencyRateProducer(KafkaTemplate<String, Currency> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCurrencyRate(Currency currency) {
        kafkaTemplate.send("currency-rates", currency);
    }
}
