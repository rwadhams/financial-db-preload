package com.wadhams.financials.db.preload.dto

import groovy.transform.ToString

@ToString(includeNames=true)
class SuncorpDTO {
	String transactionDate
	BigDecimal amount
	String payee
	String description
	String category
}
