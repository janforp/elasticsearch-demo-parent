package com.janita.search.one.util;

import com.janita.search.one.entity.Medicine;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

/**
 * Created by Janita on 2017-03-18 13:46
 */
public class JsonUtil {

    public static String obj2JsonData(Medicine medicine){
        String jsonData = null;
        try {
            //使用XContentBuilder创建json数据
            XContentBuilder jsonBuild = XContentFactory.jsonBuilder();
            jsonBuild.startObject()
                    .field("id",medicine.getId())
                    .field("name", medicine.getName())
                    .field("funciton",medicine.getFunction())
                    .endObject();
            jsonData = jsonBuild.string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonData;
    }
}
