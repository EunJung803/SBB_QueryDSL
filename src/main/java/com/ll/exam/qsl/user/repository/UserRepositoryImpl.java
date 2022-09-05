package com.ll.exam.qsl.user.repository;

import com.ll.exam.qsl.interestKeyword.entity.QInterestKeyword;
import com.ll.exam.qsl.user.entity.QSiteUser;
import com.ll.exam.qsl.user.entity.SiteUser;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.LongSupplier;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import static com.ll.exam.qsl.interestKeyword.entity.QInterestKeyword.interestKeyword;
import static com.ll.exam.qsl.user.entity.QSiteUser.siteUser;   // 구문 간소화를 위해 추가

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public SiteUser getQslUser(Long id) {
        /*
        실행하고자 하는 쿼리

        SELECT *
        FROM site_user
        WHERE id = 1
        */

        /*
        위에 있는 쿼리를 JPAQueryFactory를 사용한다면 아래와 같이 사용 가능

        return jpaQueryFactory
                .select(QSiteUser.siteUser)
                .from(QSiteUser.siteUser)
                .where(QSiteUser.siteUser.id.eq(1L))
                .fetch();
         */

        return jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .where(siteUser.id.eq(id))
                .fetchOne();
    }

    @Override
    public long getQslCount() {
        return jpaQueryFactory
                .select(siteUser.count())
                .from(siteUser)
                .fetchOne();
    }

    @Override
    public SiteUser getQslUserOrderByIdAscOne() {
        return jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .orderBy(siteUser.id.asc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public List<SiteUser> getQslUsersOrderByIdAsc() {
        return jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .orderBy(siteUser.id.asc())
                .fetch();
    }

    @Override
    public List<SiteUser> searchQsl(String kw) {
        return jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .where(
                        siteUser.username.contains(kw)
                                .or(siteUser.email.contains(kw))
                )
                .orderBy(siteUser.id.desc())
                .fetch();
    }

    @Override
    public Page<SiteUser> searchQsl(String kw, Pageable pageable) {
        JPAQuery<SiteUser> usersQuery = jpaQueryFactory
                .select(siteUser)
                .from(siteUser)
                .where(
                        siteUser.username.contains(kw)
                                .or(siteUser.email.contains(kw))
                )
                .offset(pageable.getOffset()) // 몇개를 건너 띄어야 하는지 LIMIT {1}, ?
                .limit(pageable.getPageSize()); // 한페이지에 몇개가 보여야 하는지 LIMIT ?, {1}

        // 쿼리를 알아서 구성해줌 ( ==> pageable.getSort() 에 따라서 orderBy 종류가 달라진다 )
        for (Sort.Order o : pageable.getSort()) {
            PathBuilder pathBuilder = new PathBuilder(siteUser.getType(), siteUser.getMetadata());
            usersQuery.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
        }

        // 쿼리 구성이 끝난 뒤 fetch 시켜주기
        List<SiteUser> users = usersQuery.fetch();

        LongSupplier totalSupplier = () -> 2;

        return PageableExecutionUtils.getPage(users, pageable, totalSupplier);

        // fetchCount 메서드를 사용해서, 전체 엘리먼트 개수 구하기, 하지만 더 이상 사용하면 안되는 방법
        // return new PageImpl<>(users, pageable, usersQuery.fetchCount()); // 아래와 거의 동일
        // return PageableExecutionUtils.getPage(users, pageable, usersQuery::fetchCount);
    }

    @Override
    public List<SiteUser> getQslUsersByInterestKeyword(String keywordContent) {
        /*
        # QueryDSL에 의해서 만들어져야 하는 목표 SQL
        SELECT SU.*
        FROM site_user AS SU
        INNER JOIN site_user_interest_keywords AS SUIK
        ON SU.id = SUIK.site_user_id
        INNER JOIN interest_keyword AS IK
        ON IK.content = SUIK.interest_keywords_content
        WHERE IK.content = "축구";
         */

//        QInterestKeyword IK = new QInterestKeyword("IK");

        return jpaQueryFactory
                .selectFrom(siteUser)
                .innerJoin(siteUser.interestKeywords, interestKeyword)   // INNER JOIN 하는 부분
                .where(
                        interestKeyword.content.eq(keywordContent)
                )
                .fetch();
    }

    @Override
    public List<String> getKeywordContentsByFollowingsOf(SiteUser user) {
        QSiteUser siteUser2 = new QSiteUser("siteUser2");

        return jpaQueryFactory.select(interestKeyword.content).distinct()
                .from(interestKeyword)
                .where(interestKeyword.user.in(user.getFollowings()))
                .fetch();
    }

}