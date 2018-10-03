package com.learning.house.biz.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.learning.house.common.model.House;
import com.learning.house.common.page.PageParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendService {

    private static final String HOT_HOUSE_KEY = "hot_house";

    @Autowired
    private HouseService houseService;

    public void increase(Long id) {
        Jedis jedis = new Jedis("127.0.0.1", 6379, 3000);
        try{
            jedis.zincrby(HOT_HOUSE_KEY, 1.0D, id + "");
            jedis.zremrangeByRank(HOT_HOUSE_KEY, 0, -11);// 0代表第一个元素,-1代表最后一个元素，保留热度由低到高末尾10个房产
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }finally {
            jedis.close();
        }
    }

    public List<Long> getHot() {
        Jedis jedis = new Jedis("127.0.0.1", 6379, 3000);
        try{
            Set<String> idSet = jedis.zrevrange(HOT_HOUSE_KEY, 0, -1);
            List<Long> ids = idSet.stream().map(Long::parseLong).collect(Collectors.toList());
            return ids;
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            return Lists.newArrayList();
        } finally {
            jedis.close();
        }
    }

    public List<House> getHotHouse(Integer size) {
        House query = new House();
        List<Long> list = getHot();
        list = list.subList(0, Math.min(list.size(), size));
        if (list.isEmpty()) {
            return Lists.newArrayList();
        }
        query.setIds(list);
        final List<Long> order = list;
        List<House> houses = houseService.queryAndSetImg(query, PageParams.build(size, 1));
        Ordering<House> houseSort = Ordering.natural().onResultOf(hs -> {
            return order.indexOf(hs.getId());
        });
        //Collections.sort();s
        return houseSort.sortedCopy(houses);
    }

    public List<House> getLastest() {
        House query = new House();
        query.setSort("create_time");
        List<House> houses = houseService.queryAndSetImg(query, new PageParams(8, 1));
        return houses;
    }

}
