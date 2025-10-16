package com.treatz.dispatchservice.util;

import com.treatz.dispatchservice.entity.Rider;
import com.treatz.dispatchservice.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RiderRepository riderRepository;

    @Override
    public void run(String... args) throws Exception {
        // This code will run once, every time the application starts.
        if (riderRepository.count() == 0) {
            System.out.println("No riders found in dispatch DB. Creating sample riders...");

            Rider rider1 = new Rider();
            rider1.setUserId(1L); // <-- Use the ID of the FIRST RIDER from the Auth Service
            rider1.setName("Ravi Kumar");
            rider1.setAvailable(true);

            Rider rider2 = new Rider();
            rider2.setUserId(2L); // <-- Use the ID of the SECOND RIDER from the Auth Service
            rider2.setName("Rohit Sharma");
            rider2.setAvailable(true);

            riderRepository.save(rider1);
            riderRepository.save(rider2);

            System.out.println("Sample riders created successfully.");
        }
    }
}