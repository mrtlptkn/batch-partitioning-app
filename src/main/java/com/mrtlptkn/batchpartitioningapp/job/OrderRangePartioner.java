package com.mrtlptkn.batchpartitioningapp.job;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// Bu algoritma üzerinden PartitionHandler kaç adet WorkerStep işlemi böleceği bulur ve Her bir işi
// sub step olara split eder.
@Component
public class OrderRangePartioner implements Partitioner  {

    // Aylık Bazlı Partition Yapıyorsak -> Grid Size: 12
    // Her bir ay için bir partition 12 partition
    // 1 ile 31,30,29,28 EOMonth Min: 1 Max:28 Şubat

    // PartitionIdSize -> Partition içerisinde kaç adet verş olacağı ile ilgilenir.
    // gridSize toplam verimizi kaç adet thread devredeceğiz.

    // total: 10, gridSize: 4  PartitionIdSize: YukarıYuvarla(10/4)  -> 3  Aşağı Yuvarla: 2   ->
    // partition0: 1,2,3 -> 1,2  -> 1 125.000 Idler ilk parça
    // partition1: 4,5,6 -> 3,4
    // partition2: 7,8,9 -> 5,6
    // partition3: 10 ->  7, 8
    // partition4:    -> 9,10

    // Eğer Aşağı yuvarlarsak bu durumda 5 partition 4 thread üzerinde koşmaya çalışır
    // 1 Thread diğer Threadlerin birinin partitionlardaki işini bitirmek için kuyruğa girer.

    // Hiç kuyruklama olmasın her partition kendi Thread ile çalışsın istersek bu durumda zaten YukarıYuvarla Algoritması yeterli.

    private final JdbcTemplate jdbcTemplate;

    public OrderRangePartioner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String,ExecutionContext> partitions = new HashMap<>();

        Long min = jdbcTemplate.queryForObject("SELECT MIN(order_id) FROM orders", Long.class);
        Long max = jdbcTemplate.queryForObject("SELECT MAX(order_id) FROM orders", Long.class);

        if(min == null || max == null){ // hiçbir worker çalışmaz
            return partitions;
        }

        int index = 0;
        long totalRecordsCount = max - min + 1;
        long idSize = (long)Math.ceil((double)totalRecordsCount / gridSize);

        for (long start = min; start <= max; start += idSize, index++) {
            long end = Math.min(start + idSize - 1, max); // 1 ile 125.000 -> 125.000: 250.000 -> 250:375

            String name = "partition" + index;
            ExecutionContext context = new ExecutionContext();
            context.putLong("minId", start); // 1
            context.putLong("maxId",end); // 125.000
            context.putString("partitionName", name);
            partitions.put(name, context);
        }


        return partitions;
    }
}
