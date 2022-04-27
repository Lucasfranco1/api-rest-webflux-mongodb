package com.api.rest.springboot.webflux.controllers;

import com.api.rest.springboot.webflux.documents.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import com.api.rest.springboot.webflux.service.ClientService;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import javax.validation.Valid;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Value("${config.uploads.path}")
    private String path;

    @PostMapping("/registerClientWithPhoto")
    public Mono<ResponseEntity<Client>> registerClientWithPhoto(Client client, @RequestPart FilePart file){
        client.setPhoto(UUID.randomUUID().toString()+"-"+ file.filename()
                .replace(" ", "")
                .replace(":","")
                .replace("//",""));

        return file.transferTo(new File(path + client.getPhoto()))
                .then(clientService.saveClient(client))
                .map(c -> ResponseEntity.created(URI.create("/api/clients/".concat(c.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(c));
    }

    @PostMapping("/upload/{id}")
    public Mono<ResponseEntity<Client>>uploadPhoto(@PathVariable String id, @RequestPart FilePart file){
        return clientService.findById(id).flatMap(c -> {
             c.setPhoto(UUID.randomUUID().toString()+"-"+ file.filename()
                     .replace(" ", "")
                     .replace(":","")
                     .replace("//",""));
            return file.transferTo(new File(path + c.getPhoto()))
                    .then(clientService.saveClient(c));
        }).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Client>>>AllClients(){
        return Mono.just(
                ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(clientService.findAll())
        );
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Client>>seeDetailsTheClient(@PathVariable String id){
        return clientService.findById(id).map(c -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(c))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>>saveClient(@Valid @RequestBody Mono<Client> monoClient){
        Map<String, Object>response=new HashMap<>();
        return monoClient.flatMap(client -> clientService.saveClient(client).map(c ->{
            response.put("client", c);
            response.put("message", "Client saved!");
            response.put("timestamp", new Date());
            return ResponseEntity.created(URI.create("api/clients/".concat(c.getId())))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        })).onErrorResume(t -> Mono.just(t).cast(WebExchangeBindException.class)
                .flatMap(e -> Mono.just(e.getFieldErrors()))
                .flatMapMany(Flux::fromIterable)
                .map(fieldErrors -> "Errors: "+ fieldErrors.getField()+ " "+ fieldErrors.getDefaultMessage())
                .collectList()
                .flatMap(list -> {
                    response.put("errors", list);
                    response.put("timestamp", new Date());
                    response.put("Status", HttpStatus.BAD_REQUEST.value());

                    return Mono.just(ResponseEntity.badRequest().body(response));
                }));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Client>>editClient(@RequestBody Client client, @PathVariable String id){
        return clientService.findById(id).flatMap(c -> {
            c.setName(client.getName());
            c.setLastName(client.getLastName());
            c.setAge(client.getAge());
            c.setSalary(client.getSalary());
            return clientService.saveClient(c);
        }).map(c -> ResponseEntity.created(URI.create("/api/clients/".concat(c.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(c))
                .defaultIfEmpty(ResponseEntity.notFound().build());

    }
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>>deleteClient(@PathVariable String id){
        return clientService.findById(id).flatMap(c -> clientService.deleteClient(c)
                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))))
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }




}
