package cr.ac.una.smartroute.data.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Equivalente de {@link cr.ac.una.smartroute.data.repository.BaseRepository} para el subdominio
 * documental: un solo punto donde declarar lo común a todos los repositorios de MongoDB.
 */
@NoRepositoryBean
public interface BaseMongoRepository<T, ID> extends MongoRepository<T, ID> {
}
