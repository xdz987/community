package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate template;

    public void saveDiscussPost(DiscussPost post){
        // 保存到es
        discussPostRepository.save(post);
    }

    public void deleteDiscussPost(int id){
        discussPostRepository.deleteById(id);
    }

    public Map<String,Object> searchDiscussPost(String keyword,int current,int limit){
        Map<String,Object> searchResult = new HashMap<>();
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                // 搜索字段范围
                .withQuery(QueryBuilders.multiMatchQuery(keyword,"title","content"))
                // 排序规则 type status createTime
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 分页
                .withPageable(PageRequest.of(current, limit))
                // 关键词高亮
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        SearchHits<DiscussPost> searchHits = template.search(searchQuery, DiscussPost.class);
        // 设置一个需要返回的实体类集合
        List<DiscussPost> discussPosts = new ArrayList<>();

        // 查询总结果数
        long rows = searchHits.getTotalHits();
        if (searchHits.getTotalHits() > 0) {
            for (SearchHit searchHit : searchHits) {
                DiscussPost post = (DiscussPost) searchHit.getContent();

                //处理高亮结果
                List<String> titleField = searchHit.getHighlightField("title");
                post.setTitle(titleField.isEmpty() ? post.getTitle() : titleField.get(0));
                List<String> contentField = searchHit.getHighlightField("content");
                post.setContent(contentField.isEmpty() ? post.getContent() : contentField.get(0));
                discussPosts.add(post);
            }
        }
        searchResult.put("discussPosts", discussPosts);
        searchResult.put("rows", rows);

        return searchResult;
    }
}
