package com.example.web.jobs;

import com.example.web.entity.Appoint;
import com.example.web.mapper.AppointMapper;
import com.example.web.service.AppointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderJob {

    @Autowired
    private AppointService AppointService;

    //每隔5s检测一次
    @Scheduled(cron = "0/5 * * * * ?")
    public void execute() {
        AppointService.AutoCancel();
    }
}
