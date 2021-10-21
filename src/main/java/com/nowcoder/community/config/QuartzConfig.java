package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import com.nowcoder.community.quartz.PostHotRefreshJob;
import com.nowcoder.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置 -> 数据库 ——>调用
@Configuration
public class QuartzConfig {
    // FactoryBean可简化Bean的实例化过程：
    // 1. 通过FactoryBean封装Bean的实例化过程.
    // 2. 将FactoryBean装配到Spring容器里.
    // 3. 将FactoryBean注入给其他的Bean.
    // 4. 该Bean得到的是FactoryBean所管理的对象实例.

    // 配置JobDetail
    //@Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        // 声明job的类型
        factoryBean.setJobClass(AlphaJob.class);
        // 声明Job的名字，不能与其他任务重复
        factoryBean.setName("alphaJob");
        // 声明Job的分组
        factoryBean.setGroup("alphaJobGroup");
        // 声明任务是否持久保存。即使Job Trigger不存在也存着
        factoryBean.setDurability(true);
        // Job是否可恢复
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean，CronTriggerFactoryBean) //简单与复杂Trigger，比如设置每个月最后一天亮点执行某任务则使用复杂的Trigger
    //@Bean
    // 当有多个JobDetail时，Spring会将同名形参JobDetail优先注入。所以一般保持名字一致
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000);
        // Trigger存储一些状态的map。初始化为自带默认
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    // 刷新帖子分数任务
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        // 5分钟刷新一次
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    // 刷新热榜任务
    @Bean
    public JobDetailFactoryBean postHotRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostHotRefreshJob.class);
        factoryBean.setName("postHotRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postHotRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postHotRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        // 4分钟刷新一次
        factoryBean.setRepeatInterval(1000 * 60 * 4);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}