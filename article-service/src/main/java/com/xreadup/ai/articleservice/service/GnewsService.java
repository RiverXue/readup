package com.xreadup.ai.articleservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.ai.articleservice.model.dto.GnewsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GnewsService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${article.gnews.api-key}")
    private String apiKey;
    
    @Value("${article.gnews.base-url}")
    private String baseUrl;
    
    @Value("${article.gnews.max-results:20}")
    private int maxResults;
    
    @Value("${article.gnews.language:en}")
    private String language;
    
    @Value("${article.gnews.country:us}")
    private String country;
    
    private static final Map<String, String> CATEGORY_KEYWORDS = Map.of(
        "technology", "technology OR tech OR AI OR software OR digital",
        "business", "business OR finance OR market OR economy OR corporate",
        "health", "health OR medical OR healthcare OR wellness OR disease",
        "science", "science OR research OR discovery OR study OR scientific",
        "sports", "sports OR game OR athlete OR league OR championship",
        "education", "education OR school OR university OR learning OR student",
        "environment", "environment OR climate OR pollution OR green OR sustainability"
    );

    public GnewsService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .defaultHeader("Accept", "application/json")
                .build();
        this.objectMapper = objectMapper;
    }

    private WebClient getWebClient() {
        return webClient.mutate()
                .baseUrl(baseUrl)
                .build();
    }

    public List<GnewsResponse.GnewsArticle> fetchArticlesByCategory(String category, Integer count) {
        int actualCount = (count == null || count <= 0) ? maxResults : Math.min(count, 100);
        String keyword = CATEGORY_KEYWORDS.getOrDefault(category, category);
        
        try {
            WebClient client = getWebClient();
            Mono<GnewsResponse> response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", keyword)
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
                log.info("Successfully fetched {} articles from GNews for category {}", 
                        gnewsResponse.getArticles().size(), category);
                return gnewsResponse.getArticles();
            }
            
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
}