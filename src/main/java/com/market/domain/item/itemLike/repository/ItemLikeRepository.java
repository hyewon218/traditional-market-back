package com.market.domain.item.itemLike.repository;

import com.market.domain.item.entity.Item;
import com.market.domain.item.itemLike.entity.ItemLike;
import com.market.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemLikeRepository extends JpaRepository<ItemLike, Long> {
    Optional<ItemLike> findByItemAndMember(Item item, Member user);
}
