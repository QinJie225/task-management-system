package org.example.internshipassignmentkafka.service;

import lombok.RequiredArgsConstructor;
import org.example.internshipassignmentkafka.model.DatabaseSequence;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    private final ReactiveMongoOperations reactiveMongoOperations;

    public Mono<Long> generateSequence(String seqName) {
        return reactiveMongoOperations.findAndModify(
                        query(where("_id").is(seqName)),
                        new Update().inc("seq", 1),
                        FindAndModifyOptions.options().returnNew(true).upsert(true),
                        DatabaseSequence.class
                )
                .map(DatabaseSequence::getSeq)
                .defaultIfEmpty(1L);
    }
}