package edu.wz.locks.util;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Counter{
    private int count;
    public void increment(){
        count++;
    }
    public void decrement(){
        count--;
    }
}
