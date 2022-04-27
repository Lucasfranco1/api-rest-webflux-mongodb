package com.api.rest.springboot.webflux.service;

import com.api.rest.springboot.webflux.documents.Client;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientService {

    public Flux<Client> findAll();

    public Mono<Client>findById(String id);

    public Mono<Client>saveClient(Client client);

    public Mono<Void>deleteClient(Client client);

}
