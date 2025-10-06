package ir.repository;

import ir.model.entity.ImageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImageRepository extends MongoRepository<ImageEntity, String> {
}
