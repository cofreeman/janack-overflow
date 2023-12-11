package com.example.janackoverflow.issue.service;

import com.example.janackoverflow.global.exception.BusinessLogicException;
import com.example.janackoverflow.global.exception.ExceptionCode;
import com.example.janackoverflow.issue.domain.request.CreateIssueRequestDTO;
import com.example.janackoverflow.issue.domain.response.IssueResponseDTO;
import com.example.janackoverflow.issue.domain.response.StackOverflowResponse;
import com.example.janackoverflow.issue.entity.Issue;
import com.example.janackoverflow.issue.repository.IssueRepository;
import com.example.janackoverflow.saving.repository.InputAccountRepository;
import com.example.janackoverflow.user.entity.Users;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IssueService {
    private final IssueRepository issueRepository;
    private final InputAccountRepository inputAccountRepository;

    public IssueService(IssueRepository issueRepository, InputAccountRepository inputAccountRepository) {
        this.issueRepository = issueRepository;
        this.inputAccountRepository = inputAccountRepository;
    }

    public Issue getIssue(Long issueId) {
        return issueRepository.findById(issueId).get();
    }

    // 에러 등록
    @Transactional
    public Issue createIssue(CreateIssueRequestDTO issueRequestDTO, Users users) {
        inputAccountRepository.findByUsersIdAndStatus(users.getId(),"01")
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ACCOUNT_NOT_FOUND));
        if(issueRepository.findByUsersIdAndStatus(users.getId(),"01").isPresent()){
            throw new BusinessLogicException(ExceptionCode.ERROR_EXIST);
        }
        return issueRepository.save(issueRequestDTO.toEntity(users));
    }

    // 에러 조회
    @Transactional(readOnly = true)
    public Optional<IssueResponseDTO> getIssueById(Long issueId){
        return Optional.ofNullable(issueRepository.findById(issueId).map(IssueResponseDTO::toDto)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ERROR_NOT_FOUND)));
    }

    /*// 에러 등록 시간 조회
    @Transactional(readOnly = true)
    public LocalDateTime getIssueCreatedAt(Long issueId) {
        return issueRepository.findById(issueId).map(Issue::getCreatedAt)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ERROR_NOT_FOUND));
    }*/

    // 에러 키워드로 검색
    @Transactional(readOnly = true)
    public List<StackOverflowResponse> searchByKeyword(Long issueId) throws JsonProcessingException {
        Issue issue =  issueRepository.findById(issueId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ERROR_NOT_FOUND));
        String keyword = issue.getKeyword();

        // StackOverflow API 검색 Endpoint URL
        String apiUrl = "https://api.stackexchange.com/2.3/search?pagesize=3&order=desc&sort=votes&intitle=" + keyword + "&site=stackoverflow";

        // WebClient
        WebClient webClient = WebClient.create();
        String response = webClient.get()
                .uri(apiUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 검색 결과
        List<StackOverflowResponse> searchResult = new ArrayList<>();
        if(response != null){
            // JSON 데이터 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            // 응답으로 받은 JSON 문자열을 JsonNode로 파싱
            JsonNode responseNode = objectMapper.readTree(response);

            JsonNode itemsNode = responseNode.get("items");  // "items" 키에 해당하는 값 가져오기
            for(JsonNode itemNode : itemsNode){  // 각 항목에서 "tags","link","title" 키에 해당하는 값 가져오기
                JsonNode tagsNode = itemNode.get("tags");
                JsonNode linksNode = itemNode.get("link");
                JsonNode titlesNode = itemNode.get("title");

                StackOverflowResponse item = getStackOverflowResponse(tagsNode, linksNode, titlesNode); // 가져온 값을 사용해서 객체 생성
                searchResult.add(item);  // 생성한 객체를 검색 결과 리스트에 추가
            }
        }
        return searchResult;
    }

    private static StackOverflowResponse getStackOverflowResponse(JsonNode tagsNode, JsonNode linksNode, JsonNode titlesNode) {
        List<String> tags = new ArrayList<>();
        if(tagsNode != null){
            for(JsonNode tagNode : tagsNode){
                tags.add(String.valueOf(tagNode).replace("\"","")); // 이스케이프 문자(JSON 데이터를 파싱할 때 사용) 제거
            }
        }

        String link = String.valueOf(linksNode).replace("\"","");
        String title = String.valueOf(titlesNode).replace("\"","");

        return new StackOverflowResponse(tags, link, title);
    }

    // 에러 상태 변경 (포기)
    @Transactional
    public IssueResponseDTO updateIssueStatus(Long issueId) {
        Issue issue =  issueRepository.findById(issueId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ERROR_NOT_FOUND));
        issue.updateStatus("02");
        return IssueResponseDTO.toDto(issue);
    }
}