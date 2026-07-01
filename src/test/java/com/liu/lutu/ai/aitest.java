package com.liu.lutu.ai;

import com.liu.lutu.domain.po.Result;
import com.liu.lutu.domain.vo.TravelPlanItemVo;
import com.liu.lutu.tools.TravePlanTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class aitest {
    @Autowired
    private TravePlanTool travePlanTool;

    @Test
    void test() {

        Result<List<TravelPlanItemVo>> listResult = travePlanTool.getByTripId(57L);

        System.out.println(listResult);
    }
}
