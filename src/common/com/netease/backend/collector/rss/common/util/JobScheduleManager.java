package com.netease.backend.collector.rss.common.util;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;

/**
 * 定时任务工具类
 *
 * @author LinQ
 * @version 2012-12-29
 */
public class JobScheduleManager {
    private static final Logger logger = LoggerFactory.getLogger(JobScheduleManager.class);
    private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();
    private static final String DEFAULT_GROUP_NAME = "quartzDefaultGroup";
    private static final String DEFAULT_TRIGGER_NAME = "quartzDefaultTrigger";

    private static JobScheduleManager instance = new JobScheduleManager();

    public void addJob(String jobName, Class jobClass, String expression) throws SchedulerException, ParseException {
        Scheduler scheduler = schedulerFactory.getScheduler();
        JobDetail jobDetail = new JobDetail(jobName, DEFAULT_GROUP_NAME, jobClass);
        CronTrigger cronTrigger = new CronTrigger(jobName, DEFAULT_TRIGGER_NAME);
        cronTrigger.setCronExpression(expression);
        scheduler.scheduleJob(jobDetail, cronTrigger);
        scheduler.start();
        logger.info("开始定时任务, jobName={}, jobClass={}, expression={}", new Object[]{jobName, jobClass.getSimpleName(), expression});
    }

    public static JobScheduleManager getInstance() {
        return instance;
    }

}
