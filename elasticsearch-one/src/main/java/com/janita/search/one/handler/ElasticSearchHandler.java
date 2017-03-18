package com.janita.search.one.handler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.janita.search.one.entity.Medicine;
import com.janita.search.one.util.DataFactory;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * Created by Janita on 2017-03-18 13:49
 */
public class ElasticSearchHandler {

    private Client client;

    public ElasticSearchHandler() throws UnknownHostException {
        //使用本机做为节点
        this("127.0.0.1");
    }

    public ElasticSearchHandler(String ipAddress) throws UnknownHostException {
        //集群连接超时设置
        client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));

//        client = new PreBuiltTransportClient(Settings.EMPTY)
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host1"), 9300))
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("host2"), 9300));


    }


    /**
     * 建立索引,索引建立好之后,会在elasticsearch-0.20.6\data\elasticsearch\nodes\0创建所以你看
     * @param indexName  为索引库名，一个es集群中可以有多个索引库。 名称必须为小写
     * @param type  Type为索引类型，是用来区分同索引库下不同类型的数据的，一个索引库下可以有多个索引类型。
     * @param jsonData     json格式的数据集合
     *
     * @return
     */
    public void createIndexResponse(String indexName, String type, List<String> jsonData){
        //创建索引库 需要注意的是.setRefresh(true)这里一定要设置,否则第一次建立索引查找不到数据
        IndexRequestBuilder requestBuilder = client.prepareIndex(indexName, type).setRefresh(true);
        for(int i=0; i<jsonData.size(); i++){
            requestBuilder.setSource(jsonData.get(i)).execute().actionGet();
        }
    }

    /**
     * 创建索引
     * @param jsonData
     * @return
     */
    public IndexResponse createIndexResponse(String indexName, String type,String jsonData){
        IndexResponse response = client.prepareIndex(indexName, type)
                .setSource(jsonData)
                .execute()
                .actionGet();
        return response;
    }

    /**
     * 执行搜索
     * @param queryBuilder
     * @param indexName
     * @param type
     * @return
     */
    public List<Medicine>  searcher(QueryBuilder queryBuilder, String indexName, String type){
        List<Medicine> list = new ArrayList<Medicine>();
        SearchResponse searchResponse = client.prepareSearch(indexName).setTypes(type)
                .setQuery(queryBuilder)
                .execute()
                .actionGet();
        SearchHits hits = searchResponse.getHits();
        System.out.println("查询到记录数=" + hits.getTotalHits());
        SearchHit[] searchHists = hits.getHits();
        if(searchHists.length>0){
            for(SearchHit hit:searchHists){
                Integer id = (Integer)hit.getSource().get("id");
                String name =  (String) hit.getSource().get("name");
                String function =  (String) hit.getSource().get("funciton");
                list.add(new Medicine(id, name, function));
            }
        }
        return list;
    }


    public static void main(String[] args) throws UnknownHostException {
        ElasticSearchHandler esHandler = new ElasticSearchHandler();
        List<String> jsonData = DataFactory.getInitJsonData();
        String indexName = "indexdemo";
        String type = "typedemo";
        esHandler.createIndexResponse(indexName, type, jsonData);
        //查询条件
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("name", "感冒");
        List<Medicine> result = esHandler.searcher(queryBuilder, indexName, type);
        for(int i=0; i<result.size(); i++){
            Medicine medicine = result.get(i);
            System.out.println("(" + medicine.getId() + ")药品名称:" +medicine.getName() + "\t\t" + medicine.getFunction());
        }
    }
}
