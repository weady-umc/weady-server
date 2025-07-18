package com.weady.weady.domain.board.service;

import com.weady.weady.domain.board.dto.BoardRequest;
import com.weady.weady.domain.board.dto.BoardResponse;
import com.weady.weady.domain.board.entity.Board;
import com.weady.weady.domain.board.entity.BoardImg;
import com.weady.weady.domain.board.entity.BoardPlace;
import com.weady.weady.domain.board.repository.BoardRepositoryImpl;
import com.weady.weady.global.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepositoryImpl boardRepositoryImpl;
    private final S3Uploader s3Uploader;

    public BoardResponse.BoardResponseDTO createPost
            (List<MultipartFile> images, BoardRequest.BoardCreateRequestDTO request) throws IOException {

        // 1. 게시물 엔티티에 postData 저장
        Board board = Board.builder()
                .isPublic(true)
                .content(request.content())
                .build();
        boardRepositoryImpl.save(board);

        // 장소 리스트로 받아서 -> BoardPlace 엔티티로 변환
        List<BoardPlace> places = request.boardPlaceRequestDTOList().stream()
                .map(dto -> BoardPlace.builder()
                        .placeName(dto.placeName())
                        .placeAddress(dto.placeAddress())
                        .board(board)   // 연관관계 설정!!
                        .build())
                .collect(Collectors.toList());
        boardRepositoryImpl.saveAll(places);

        // 2. 응답 DTO
        List<BoardResponse.BoardPlaceResponseDTO> placeList = places.stream()
                .map(place -> BoardResponse.BoardPlaceResponseDTO.builder()
                        .placeName(place.getPlaceName())
                        .placeAddress(place.getPlaceAddress())
                        .build())
                .collect(Collectors.toList());



        // 2. 이미지 s3에 저장 후 url, 순서 DB에 저장,
        List<BoardResponse.BoardImgResponseDTO> imgList = new ArrayList<>();

        if (images != null) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                String imageUrl = s3Uploader.upload(image, "board-images"); //url 받음

                // 이미지 엔티티 생성
                BoardImg boardImg = BoardImg.builder()
                        .board(board) // 연관관계 설정
                        .imgOrder(i + 1) //이미지 순서 저장
                        .imgUrl(imageUrl)
                        .build();

                // 응답 DTO 리스트에 추가
                imgList.add(
                        BoardResponse.BoardImgResponseDTO.builder()
                                .imgOrder(i + 1)
                                .imgUrl(imageUrl)
                                .build()
                );
            }
        }
        boardRepositoryImpl.saveAll(images);

        // 3. 응답 DTO 생성
        BoardResponse.BoardResponseDTO responseDTO = BoardResponse.BoardResponseDTO.builder()
                .boardId(board.getId())
                .isPublic(board.isPublic())
                .imageDTOList(imgList)
                .placeDTOList(placeList)
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .build();

        return responseDTO;

    }

    /*
    private String convertWeatherCodeToString(Integer code) {
        // 예: 1 → "sunny", 2 → "rainy" 등 변환 로직 구현
        if (code == null) return null;
        switch (code) {
            case 1: return "sunny";
            case 2: return "rainy";
            case 3: return "cloudy";
            default: return "unknown";
        }
    }

     */
}
