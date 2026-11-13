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
    private final OrderRangePartioner partitionHandler;

    public OrderSeedController(OrderSeed orderSeed, OrderRangePartioner partitionHandler) {
        this.orderSeed = orderSeed;
        this.partitionHandler = partitionHandler;
    }


    @PostMapping("/seed")
    public ResponseEntity<String> seed(@RequestParam("count") int count) {
        int writtenCount =  orderSeed.Seed(count);
        return ResponseEntity.ok("Seed completed successfully." + writtenCount);
    }

    @PostMapping("/partitionTest")
    public ResponseEntity<String> test(@RequestParam("gridSize") int gridSize){
        var response =  partitionHandler.partition(gridSize);

        return ResponseEntity.ok("Partition test completed successfully." + response);

    }

}
