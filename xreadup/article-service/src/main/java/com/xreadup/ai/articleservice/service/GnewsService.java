package com.xreadup.ai.articleservice.service;

import com.xreadup.ai.articleservice.model.dto.GnewsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class GnewsService {

    private final WebClient webClient;
    
    @Value("${article.gnews.api-key}")
    private String apiKey;
    
    @Value("${article.gnews.base-url}")
    private String baseUrl;
    
    @Value("${article.gnews.max-results:100}")
    private int maxResults;
    
    @Value("${article.gnews.language:en}")
    private String language;
    
    @Value("${article.gnews.country:us}")
    private String country;
    
    // GNews API官方支持的分类列表
    private static final Set<String> SUPPORTED_CATEGORIES = Set.of(
        "general", "world", "nation", "business", "technology", 
        "entertainment", "sports", "science", "health"
    );
    
    // 支持的语言代码
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of(
        "en", "zh", "es", "fr", "de", "it", "pt", "ru", "ja", "ko", "ar", "hi"
    );
    
    // 支持的国家代码
    private static final Set<String> SUPPORTED_COUNTRIES = Set.of(
        "us", "gb", "ca", "au", "in", "de", "fr", "it", "es", "br", "mx", "jp", "kr", "cn", "ru"
    );
    
    // 支持的排序方式
    private static final Set<String> SUPPORTED_SORT_OPTIONS = Set.of(
        "publishedAt", "relevance"
    );

    public GnewsService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .defaultHeader("Accept", "application/json")
                .build();
    }

    private WebClient getWebClient() {
        return webClient.mutate()
                .baseUrl(baseUrl)
                .build();
    }

    public List<GnewsResponse.GnewsArticle> fetchArticlesByCategory(String category, Integer count) {
        return fetchArticlesByCategory(category, count, language, country, null, null, null);
    }
    
    /**
     * 根据分类获取文章（增强版，支持多语言、多国家、时间范围、排序）
     * @param category 分类
     * @param count 文章数量
     * @param lang 语言代码
     * @param countryCode 国家代码
     * @param fromDate 开始日期 (格式: 2024-01-01T00:00:00Z)
     * @param toDate 结束日期 (格式: 2024-12-31T23:59:59Z)
     * @param sortBy 排序方式 (publishedAt, relevance)
     * @return 文章列表
     */
    public List<GnewsResponse.GnewsArticle> fetchArticlesByCategory(String category, Integer count, 
            String lang, String countryCode, String fromDate, String toDate, String sortBy) {
        int actualCount = (count == null || count <= 0) ? maxResults : Math.min(count, 100);
        
        // 验证分类是否被支持
        if (!SUPPORTED_CATEGORIES.contains(category)) {
            log.warn("Unsupported category: {}. Supported categories: {}", category, SUPPORTED_CATEGORIES);
            return Collections.emptyList();
        }
        
        // 使用传入的参数，如果为null则使用默认值
        String actualLang = (lang != null && !lang.isEmpty()) ? lang : language;
        String actualCountry = (countryCode != null && !countryCode.isEmpty()) ? countryCode : country;
        
        log.info("GNews API call parameters: category={}, lang={}, country={}, from={}, to={}, sortBy={}, count={}", 
                category, actualLang, actualCountry, fromDate, toDate, sortBy, actualCount);
        
        try {
            WebClient client = getWebClient();
            
            // 构建查询参数
            var uriBuilder = client.get()
                    .uri(uri -> {
                        var builder = uri
                                .path("/top-headlines")
                                .queryParam("category", category)
                                .queryParam("lang", actualLang)
                                .queryParam("country", actualCountry)
                                .queryParam("max", actualCount)
                                .queryParam("apikey", apiKey);
                        
                        // 添加可选参数
                        if (fromDate != null && !fromDate.isEmpty()) {
                            builder.queryParam("from", fromDate);
                        }
                        if (toDate != null && !toDate.isEmpty()) {
                            builder.queryParam("to", toDate);
                        }
                        if (sortBy != null && !sortBy.isEmpty()) {
                            builder.queryParam("sortby", sortBy);
                        }
                        
                        return builder.build();
                    });
            
            Mono<GnewsResponse> response = uriBuilder
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("GNews API error: {}", clientResponse.statusCode());
                                return Mono.error(new RuntimeException("GNews API error: " + clientResponse.statusCode()));
                            })
                    .bodyToMono(GnewsResponse.class)
                    .timeout(Duration.ofSeconds(30));

            GnewsResponse gnewsResponse = response.block();
            
            if (gnewsResponse != null && gnewsResponse.getArticles() != null) {
                log.info("Successfully fetched {} articles from GNews for category {}", 
                        gnewsResponse.getArticles().size(), category);
                
                if (gnewsResponse.getArticles().size() < actualCount) {
                    log.warn("GNews returned fewer articles than requested: requested={}, returned={}, category={}", 
                            actualCount, gnewsResponse.getArticles().size(), category);
                }
                
                return gnewsResponse.getArticles();
            }
            
            log.warn("GNews API returned empty response for category: {}", category);
            return Collections.emptyList();
            
        } catch (WebClientResponseException e) {
            log.error("WebClient error fetching articles: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching articles from GNews: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<GnewsResponse.GnewsArticle> fetchTopHeadlines(Integer count) {
        int actualCount = (count == null || count <= 0) ? maxResults : Math.min(count, 100);
        
        try {
            WebClient client = getWebClient();
            Mono<GnewsResponse> response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/top-headlines")
                            .queryParam("lang", language)
                            .queryParam("country", country)
                            .queryParam("max", actualCount)
                            .queryParam("apikey", apiKey)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("GNews API error: {}", clientResponse.statusCode());
                                return Mono.error(new RuntimeException("GNews API error: " + clientResponse.statusCode()));
                            })
                    .bodyToMono(GnewsResponse.class)
                    .timeout(Duration.ofSeconds(30));

            GnewsResponse gnewsResponse = response.block();
            
            if (gnewsResponse != null && gnewsResponse.getArticles() != null) {
                log.info("Successfully fetched {} top headlines from GNews", gnewsResponse.getArticles().size());
                return gnewsResponse.getArticles();
            }
            
            return Collections.emptyList();
            
        } catch (WebClientResponseException e) {
            log.error("WebClient error fetching top headlines: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching top headlines from GNews: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取GNews API支持的分类列表
     * @return 支持的分类集合
     */
    public Set<String> getSupportedCategories() {
        return SUPPORTED_CATEGORIES;
    }
    
    /**
     * 检查分类是否被支持
     * @param category 分类名称
     * @return 是否支持
     */
    public boolean isCategorySupported(String category) {
        return SUPPORTED_CATEGORIES.contains(category);
    }
    
    /**
     * 获取支持的语言列表
     * @return 支持的语言代码集合
     */
    public Set<String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }
    
    /**
     * 获取支持的国家列表
     * @return 支持的国家代码集合
     */
    public Set<String> getSupportedCountries() {
        return SUPPORTED_COUNTRIES;
    }
    
    /**
     * 获取支持的排序选项
     * @return 支持的排序方式集合
     */
    public Set<String> getSupportedSortOptions() {
        return SUPPORTED_SORT_OPTIONS;
    }
    
    /**
     * 检查语言是否被支持
     * @param language 语言代码
     * @return 是否支持
     */
    public boolean isLanguageSupported(String language) {
        return SUPPORTED_LANGUAGES.contains(language);
    }
    
    /**
     * 检查国家是否被支持
     * @param country 国家代码
     * @return 是否支持
     */
    public boolean isCountrySupported(String country) {
        return SUPPORTED_COUNTRIES.contains(country);
    }
    
    /**
     * 检查排序选项是否被支持
     * @param sortBy 排序方式
     * @return 是否支持
     */
    public boolean isSortOptionSupported(String sortBy) {
        return SUPPORTED_SORT_OPTIONS.contains(sortBy);
    }
    
    /**
     * 根据关键词搜索文章（用于自定义主题）
     * @param keyword 搜索关键词
     * @param count 文章数量
     * @return 文章列表
     */
    public List<GnewsResponse.GnewsArticle> searchArticlesByKeyword(String keyword, Integer count) {
        return searchArticlesByKeyword(keyword, count, language, country, null, null, null);
    }
    
    /**
     * 根据关键词搜索文章（增强版，支持多语言、多国家、时间范围、排序）
     * @param keyword 搜索关键词
     * @param count 文章数量
     * @param lang 语言代码
     * @param countryCode 国家代码
     * @param fromDate 开始日期 (格式: 2024-01-01T00:00:00Z)
     * @param toDate 结束日期 (格式: 2024-12-31T23:59:59Z)
     * @param sortBy 排序方式 (publishedAt, relevance)
     * @return 文章列表
     */
    public List<GnewsResponse.GnewsArticle> searchArticlesByKeyword(String keyword, Integer count, 
            String lang, String countryCode, String fromDate, String toDate, String sortBy) {
        int actualCount = (count == null || count <= 0) ? maxResults : Math.min(count, 100);
        
        // 使用传入的参数，如果为null则使用默认值
        String actualLang = (lang != null && !lang.isEmpty()) ? lang : language;
        String actualCountry = (countryCode != null && !countryCode.isEmpty()) ? countryCode : country;
        
        log.info("GNews API search parameters: keyword={}, lang={}, country={}, from={}, to={}, sortBy={}, count={}", 
                keyword, actualLang, actualCountry, fromDate, toDate, sortBy, actualCount);
        
        try {
            WebClient client = getWebClient();
            
            // 构建查询参数
            var uriBuilder = client.get()
                    .uri(uri -> {
                        var builder = uri
                                .path("/search")
                                .queryParam("q", keyword)
                                .queryParam("lang", actualLang)
                                .queryParam("country", actualCountry)
                                .queryParam("max", actualCount)
                                .queryParam("apikey", apiKey);
                        
                        // 添加可选参数
                        if (fromDate != null && !fromDate.isEmpty()) {
                            builder.queryParam("from", fromDate);
                        }
                        if (toDate != null && !toDate.isEmpty()) {
                            builder.queryParam("to", toDate);
                        }
                        if (sortBy != null && !sortBy.isEmpty()) {
                            builder.queryParam("sortby", sortBy);
                        }
                        
                        return builder.build();
                    });
            
            Mono<GnewsResponse> response = uriBuilder
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("GNews API search error: {}", clientResponse.statusCode());
                                return Mono.error(new RuntimeException("GNews API search error: " + clientResponse.statusCode()));
                            })
                    .bodyToMono(GnewsResponse.class)
                    .timeout(Duration.ofSeconds(30));

            GnewsResponse gnewsResponse = response.block();
            
            if (gnewsResponse != null && gnewsResponse.getArticles() != null) {
                log.info("Successfully fetched {} articles from GNews for keyword: {}", 
                        gnewsResponse.getArticles().size(), keyword);
                
                if (gnewsResponse.getArticles().size() < actualCount) {
                    log.warn("GNews returned fewer articles than requested: requested={}, returned={}, keyword={}", 
                            actualCount, gnewsResponse.getArticles().size(), keyword);
                }
                
                return gnewsResponse.getArticles();
            }
            
            log.warn("GNews API returned empty response for keyword: {}", keyword);
            return Collections.emptyList();
            
        } catch (WebClientResponseException e) {
            log.error("WebClient error searching articles: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error searching articles from GNews: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}