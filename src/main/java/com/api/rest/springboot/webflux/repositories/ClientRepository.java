package com.api.rest.springboot.webflux.repositories;

import com.api.rest.springboot.webflux.documents.Client;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ClientRepository extends ReactiveMongoRepository<Client,String> {
}
