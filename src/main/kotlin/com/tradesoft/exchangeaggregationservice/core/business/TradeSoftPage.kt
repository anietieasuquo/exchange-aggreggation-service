package com.tradesoft.exchangeaggregationservice.core.business

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

@JsonIgnoreProperties(ignoreUnknown = true, value = ["pageable"])
class TradeSoftPage<T> @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    @JsonProperty("content") content: List<T>,
    @JsonProperty("number") page: Int,
    @JsonProperty("size") size: Int,
    @JsonProperty("numberOfElements") total: Long,
) : PageImpl<T>(content, PageRequest.of(page, size), total) {
    constructor(page: Page<T>) : this(page.content, page.number, page.size, page.numberOfElements.toLong())
    constructor() : this(emptyList(), 0, 0, 0)
}
