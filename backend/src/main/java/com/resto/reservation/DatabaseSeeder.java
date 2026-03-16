package com.resto.reservation;

import com.resto.reservation.entity.RestaurantTable;
import com.resto.reservation.repository.RestaurantTableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(DatabaseSeeder.class.getName());

    private final RestaurantTableRepository tableRepo;

    public DatabaseSeeder(RestaurantTableRepository tableRepo) {
        this.tableRepo = tableRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (tableRepo.count() == 0) { // dont add if db isn't empty
            RestaurantTable table1 = new RestaurantTable();
            table1.setTableNumber(2);
            table1.setCapacity(4);

            RestaurantTable table2 = new RestaurantTable();
            table2.setTableNumber(1);
            table2.setCapacity(6);

            tableRepo.save(table1);
            tableRepo.save(table2);

            LOGGER.log(Level.INFO, "Dummy data added to database");
        }
    }
}
