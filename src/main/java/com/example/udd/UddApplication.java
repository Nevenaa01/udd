package com.example.udd;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import io.minio.messages.Bucket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class UddApplication {

	public static void main(String[] args) {
		MinioClient minioClient = demo();
		try{
			List<Bucket> buckets = minioClient.listBuckets();
			System.out.println("Connection success, total buckets: " + buckets.size());
		} catch (MinioException e){
			System.out.println("Connection failed: " + e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
		}

		SpringApplication.run(UddApplication.class, args);
	}

	private static MinioClient demo(){
		MinioClient minioClient = MinioClient.builder()
				.endpoint("http://localhost:9000")
				.credentials("SVDdqWLflOqzWpVRqT2T", "lKzjUJ2VYYfUFx86YuP8rJLl6PcrnTWyLsxRF971")
				.build();

		return minioClient;
	}

}
