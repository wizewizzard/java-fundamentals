package edu.wz.queues;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.assertj.core.error.array2d.Array2dElementShouldBeDeepEqual;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class QueuesTest {

    @Data
    @AllArgsConstructor
    public static class Report{
        private int priority;
        private String data;
    }

    /**
     * Based on comparator given when constructing the queue elements will be ordered. Unfortunately queue does not
     * respect the order in which the elements with the same priority were inserted.
     */
    @Test
    public void priorityQueue(){
        Queue<Report> queue = new PriorityQueue<>(Comparator.comparing(Report::getPriority).reversed());

        queue.add(new Report(1, "Data-1"));
        queue.add(new Report(10, "Data1"));
        queue.add(new Report(10, "Data2"));
        queue.add(new Report(3, "Data3"));
        queue.add(new Report(1, "Data4"));
        queue.add(new Report(3, "Data5"));
        queue.add(new Report(3, "Data6"));
        queue.add(new Report(5, "Data7"));
        queue.add(new Report(6, "Data8"));
        queue.add(new Report(10, "Data0"));

        assertThat(queue)
            .isNotEmpty();
        int pr = queue.poll().getPriority();
        while(!queue.isEmpty()){
            int curPr = queue.poll().getPriority();
            assertThat(curPr).isLessThanOrEqualTo(pr);
            pr = curPr;
        }

    }
}
