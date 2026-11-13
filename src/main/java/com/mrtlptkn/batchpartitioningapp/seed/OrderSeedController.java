package com.mrtlptkn.batchpartitioningapp.seed;


import com.mrtlptkn.batchpartitioningapp.job.OrderRangePartioner;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/orders")
public class OrderSeedController {

    private final OrderSeed orderSeed;


    public OrderSeedController(OrderSeed orderSeed) {
        this.orderSeed = orderSeed;
    }


    @PostMapping("/seed")
    public ResponseEntity<String> seed(@RequestParam("count") int count) {
        int writtenCount =  orderSeed.Seed(count);
        return ResponseEntity.ok("Seed completed successfully." + writtenCount);
    }



}
