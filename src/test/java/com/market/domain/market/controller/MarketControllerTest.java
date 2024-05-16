package com.market.domain.market.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.domain.market.dto.MarketRequestDto;
import com.market.domain.market.entity.Market;
import com.market.domain.market.repository.MarketRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class MarketControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    MarketRepository marketRepository;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .build();
        marketRepository.deleteAll();
    }

    @DisplayName("createMarket: 시장 생성에 성공한다.")
    @Test
    public void createMarket() throws Exception {
        // given
        final String url = "/api/markets";

        final String marketName = "수유시장";
        final String marketAddr = "수유동 어쩌구";
        final String marketDetail = "추가 설명";

        final MarketRequestDto requestDto = new MarketRequestDto(marketName, marketAddr,
            marketDetail);

        // 객체 JSON 으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(requestDto);

        final MockMultipartFile marketRequest = new MockMultipartFile(
            "dto",
            "dto",
            MediaType.APPLICATION_JSON_VALUE,
            requestBody.getBytes(StandardCharsets.UTF_8));

        final MockMultipartFile file = new MockMultipartFile(
            "imageFiles",
            "elb.png",
            MediaType.IMAGE_PNG_VALUE,
            (byte[]) null
        );

        // when
        // 설정한 내용을 바탕으로 요청 전송
        ResultActions result = mockMvc.perform(multipart(url)
            .file(file)
            .file(marketRequest)
        );

        // then
        result.andExpect(status().isCreated());

        List<Market> markets = marketRepository.findAll();

        assertThat(markets.size()).isEqualTo(1); // 크기가 1인지 검증
        assertThat(markets.get(0).getMarketName()).isEqualTo(marketName);
        assertThat(markets.get(0).getMarketAddr()).isEqualTo(marketAddr);
        assertThat(markets.get(0).getMarketDetail()).isEqualTo(marketDetail);
    }

    @DisplayName("getMarkets: 시장 목록 조회에 성공한다.")
    @Test
    public void getMarkets() throws Exception {
        // given
        final String url = "/api/markets";


        marketRepository.save(Market.builder()
            .marketName("속초시장")
            .marketAddr("속초동 어쩌구")
            .marketDetail("속초시장 추가 설명")
            .build());

        marketRepository.save(Market.builder()
            .marketName("부평시장")
            .marketAddr("부평동 어쩌구")
            .marketDetail("부평시장 추가 설명")
            .build());

        marketRepository.save(Market.builder()
            .marketName("수유시장")
            .marketAddr("수유동 어쩌구")
            .marketDetail("수유시장 추가 설명")
            .build());

        marketRepository.save(Market.builder()
            .marketName("광장시장")
            .marketAddr("예지동 어쩌구")
            .marketDetail("광장시장 추가 설명")
            .build());

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "marketName"));
        Page<Market> marketPages = marketRepository.findAll(pageRequest);

        // when
        // 설정한 내용을 바탕으로 요청 전송
        mockMvc.perform(get(url)
            .accept(MediaType.APPLICATION_JSON));

        List<Market> content = marketPages.getContent(); // 조회된 데이터

        assertThat(content.size()).isEqualTo(3); // 조회된 데이터 수
        assertThat(marketPages.getTotalElements()).isEqualTo(4); // 전체 데이터 수
        assertThat(marketPages.getNumber()).isEqualTo(0); // 페이지 번호
        assertThat(marketPages.getTotalPages()).isEqualTo(2); // 전체 페이지 번호
        assertThat(marketPages.isFirst()).isTrue(); // 첫번째 항목인가?
        assertThat(marketPages.hasNext()).isTrue(); // 다음 페이지가 있는가?



    }

    @DisplayName("updateMarket: 시장 수정에 성공한다.")
    @Test
    public void updateMarket() throws Exception {
        // given
        final String url = "/api/markets/{no}";

        final String marketName = "속초시장";
        final String marketAddr = "속초동 어쩌구";
        final String marketDetail = "속초시장 추가 설명";


        Market savedMarket = marketRepository.save(Market.builder()
            .marketName(marketName)
            .marketAddr(marketAddr)
            .marketDetail(marketDetail)
            .build());

        final String newMarketName = "부평시장";
        final String newMarketAddr = "부평동 어쩌구";
        final String newMarketDetail = "부평시장 추가 설명";

        final MarketRequestDto requestDto = new MarketRequestDto(newMarketName, newMarketAddr,
            newMarketDetail);

        // 객체 JSON 으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(requestDto);

        final MockMultipartFile marketRequest = new MockMultipartFile(
            "dto",
            "dto",
            MediaType.APPLICATION_JSON_VALUE,
            requestBody.getBytes(StandardCharsets.UTF_8));

        final MockMultipartFile file = new MockMultipartFile(
            "imageFiles",
            "elb.png",
            MediaType.IMAGE_PNG_VALUE,
            (byte[]) null
        );


        // when
        // 설정한 내용을 바탕으로 요청 전송
        ResultActions result = mockMvc.perform(multipart(HttpMethod.PUT ,url, savedMarket.getNo())
            .file(file)
            .file(marketRequest)
        );

        // then
        result.andExpect(status().isOk());

        Market market = marketRepository.findById(savedMarket.getNo()).get();

        assertThat(market.getMarketName()).isEqualTo(newMarketName);
        assertThat(market.getMarketAddr()).isEqualTo(newMarketAddr);
        assertThat(market.getMarketDetail()).isEqualTo(newMarketDetail);
    }

    @DisplayName("deleteMarket: 시장 삭제에 성공한다.")
    @Test
    public void deleteMarket() throws Exception {
        // given
        final String url = "/api/markets/{no}";

        final String marketName = "속초시장";
        final String marketAddr = "속초동 어쩌구";
        final String marketDetail = "속초시장 추가 설명";


        Market savedMarket = marketRepository.save(Market.builder()
            .marketName(marketName)
            .marketAddr(marketAddr)
            .marketDetail(marketDetail)
            .build());

        // when
        // 설정한 내용을 바탕으로 요청 전송
        ResultActions result = mockMvc.perform(multipart(HttpMethod.DELETE ,url, savedMarket.getNo())
        );

        // then
        result.andExpect(status().isOk());

        List<Market> markets = marketRepository.findAll();

        assertThat(markets).isEmpty();
    }

}