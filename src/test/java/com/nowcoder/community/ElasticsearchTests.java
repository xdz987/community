package com.nowcoder.community;

import com.nowcoder.community.config.ElasticSearchClientConfig;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {
    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ElasticSearchClientConfig elasticSearchClientConfig;

    @Test
    public void testInsert() {
        discussRepository.save(discussMapper.selectDiscussPostById(241));
        discussRepository.save(discussMapper.selectDiscussPostById(242));
        discussRepository.save(discussMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussRepository.saveAll(discussMapper.selectDiscussPosts(101, 0, 100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(102, 0, 100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(103, 0, 100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(111, 0, 100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(112, 0, 100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(131, 0, 100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(132, 0, 100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(133, 0, 100,0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(134, 0, 100,0));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使劲灌水。");
        discussRepository.save(post);
    }

    @Test
    public void testDelete() {
//        discussRepository.deleteById(231);
        discussRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                // 搜索字段范围
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 排序规则 type status createTime
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 分页
                .withPageable(PageRequest.of(0, 10))
                // 关键词高亮
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        // 当前页帖子的集合
        // 拿到的是原始集合数据，没有高亮，底层获取得到了高亮显示的值，但是没有返回
        // 底层：elasticTemplate.queryForPage(searchQuery,class,SearchResultMapper)
        Page<DiscussPost> page = discussRepository.search(searchQuery);
        System.out.println(page.getTotalPages());
        System.out.println(page.getTotalElements());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }

    @Test
    public void testSearchByTemplate(){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                // 搜索字段范围
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                // 排序规则 type status createTime
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 分页
                .withPageable(PageRequest.of(0, 10))
                // 关键词高亮
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> searchHits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        // 查询总结果数
        System.out.println(searchHits.getTotalHits());
        if (searchHits.getTotalHits() > 0) {
            // 设置一个需要返回的实体类集合
            List<DiscussPost> discussPosts = new ArrayList<>();

            for (SearchHit<DiscussPost> searchHit : searchHits) {
                DiscussPost post = (DiscussPost) searchHit.getContent();

                //处理高亮结果
                List<String> titleField = searchHit.getHighlightField("title");
                System.out.println(titleField);
                post.setTitle(titleField.isEmpty()?post.getTitle():titleField.get(0));
                List<String> contentField = searchHit.getHighlightField("content");
                post.setContent(contentField.isEmpty()?post.getContent():contentField.get(0));

                discussPosts.add(post);
            }
            for (DiscussPost post : discussPosts) {
                System.out.println(post.toString());
            }
        }

        // 如匹配多段关键词，只需要第一段匹配的(参考百度：<b>互联网</b>是打大师傅，[是是去互联网])
    }
//    @Test
//    public void testSearchByClient() throws IOException {
//        // 设置一个需要返回的实体类集合
//        List<DiscussPost> discussPosts = new ArrayList<>();
//
//        // 条件搜索
//        SearchRequest searchRequest = new SearchRequest("discusspost");
//
//        // 指定检索条件
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
//        sourceBuilder.query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"));
//        // 结果排序
//        sourceBuilder.sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC));
//        // 高亮
//        HighlightBuilder highlightBuilder = new HighlightBuilder();
//        highlightBuilder.field("title").field("content").preTags("<em>").postTags("</em>");
//        sourceBuilder.highlighter(highlightBuilder);
//        // 结果分页
//        sourceBuilder.from(0).size(10);
//
//        // 执行搜索
//        searchRequest.source(sourceBuilder);
//        SearchResponse searchResponse = elasticSearchClientConfig.restHighLevelClient().search(searchRequest, RequestOptions.DEFAULT);
//        System.out.println(searchResponse);
//
//        ArrayList<Map<String,Object>> list = new ArrayList<>();
//        for (SearchHit documentFields:searchResponse.getHits()){
//            //获取高亮字段
//            Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
//            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();//原来的结果
//            HighlightField title = highlightFields.get("title");
//            HighlightField content = highlightFields.get("content");
//            //解析高亮字段,将原来的字段换为我们高亮字段即可
//            if(title!=null){
//                Text[] fragments = title.fragments();
//                String n_title="";
//                for(Text text:fragments){
//                    n_title += text;
//                }
//                sourceAsMap.put("title",n_title);//高亮字段替换原来的内容
//            }
//            //解析高亮字段,将原来的字段换为我们高亮字段即可
//            if(content!=null){
//                Text[] fragments = content.fragments();
//                String n_content="";
//                for(Text text:fragments){
//                    n_content += text;
//                }
//                sourceAsMap.put("content",n_content);//高亮字段替换原来的内容
//            }
//            list.add(sourceAsMap);
//        }
//        System.out.println(list);
//
//        // 如匹配多段关键词，只需要第一段匹配的(参考百度：<b>互联网</b>是打大师傅，[是是去互联网])
//    }
}
