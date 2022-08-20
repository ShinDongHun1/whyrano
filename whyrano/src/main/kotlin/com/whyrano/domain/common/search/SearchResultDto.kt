package com.whyrano.domain.common.search

/**
 * Created by ShinD on 2022/08/15.
 */
data class SearchResultDto<T>(

    val totalPage: Int,         // 전체 페이지 수

    val totalElementCount: Long,   // 전체 요소 수

    val currentPage: Int,       // 현재 페이지가 몇 페이지인지

    val currentElementCount: Int,  // 현재 페이지에 요소가 몇개 들어있는지

    val simpleDtos: List<T> = emptyList(),

    )