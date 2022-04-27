package com.api.rest.springboot.webflux.service;

import com.api.rest.springboot.webflux.documents.Client;
import com.api.rest.springboot.webflux.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;


    @Override
    public Flux<Client> findAll() {
        return clientRepository.findAll();
    }

    @Override
    public Mono<Client> findById(String id) {
        return clientRepository.findById(id);
    }

    @Override
    public Mono<Client> saveClient(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public Mono<Void> deleteClient(Client client) {
        return clientRepository.delete(client);
    }
}
