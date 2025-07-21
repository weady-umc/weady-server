package com.weady.weady.domain.board.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.weady.weady.domain.board.entity.board.Board;
import com.weady.weady.domain.board.entity.board.QBoard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {

    private final JpaBoardRepository jpaBoardRepository;
    private final JPAQueryFactory jpaQueryFactory;
    private final QBoard board = QBoard.board;

    @Override
    public Optional<Board> findById(Long id) {return jpaBoardRepository.findById(id);}

    @Override
    public Board save(Board board) {return jpaBoardRepository.save(board);}


    @Override
    public Slice<Board> getFilteredAndSortedResults(Long weatherTagId, Long seasonTagId, Long cursor, Pageable pageable) {
        BooleanBuilder filterBuilder = new BooleanBuilder();
        OrderSpecifier<?> orderSpecifier = board.createdAt.desc();

        filterBuilder.and(board.isPublic.isTrue());

        if (weatherTagId != null) {
            filterBuilder.and(board.weatherTag.id.eq(weatherTagId));
        }

        if (seasonTagId != null) {
            filterBuilder.and(board.seasonTag.id.eq(seasonTagId));
        }

        // cursor 값이 작아져야 함(최신순이기 때문에 boardId가 큰 순서대로 보내줌)
        if (cursor != null) {
            filterBuilder.and(board.id.lt(cursor));
        }


        List<Board> results = jpaQueryFactory
                .selectFrom(board)
                .where(filterBuilder)
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();
        if (hasNext) {
            results.remove(pageable.getPageSize());

        }
        return new SliceImpl<>(results, pageable, hasNext);
    }

}
