package com.metoo.nrsm.core.config.juc.thread.method.exercissel;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/ttt")
public class Ticket {


    @RequestMapping("/ttt")
    public void ttt(){

    }



    static Random random = new Random();

    public static int random(int amount){
        return random.nextInt(amount) + 1;
    }
}

class TicketWindow{

    private int count;

    public TicketWindow(int count) {
        this.count = count;
    }

    public int getCount(){
        return this.count;
    }

    public synchronized int sell(int amout){
        if(this.count >= amout){
            this.count -= amout;
            return amout;
        }else{
            return 0;
        }
    }
}

