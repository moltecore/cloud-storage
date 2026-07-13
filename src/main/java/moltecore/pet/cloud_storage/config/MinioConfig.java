package moltecore.pet.cloud_storage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("admin", "password123")
                .build();
    }

    @Bean
    public ApplicationRunner initMinio(MinioClient minioClient) {
        return args -> {

            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket("user-files").build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("user-files").build());
            }
        };
    }
}
