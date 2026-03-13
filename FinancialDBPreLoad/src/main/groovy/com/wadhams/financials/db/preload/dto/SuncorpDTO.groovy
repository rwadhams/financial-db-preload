package com.wadhams.financials.db.preload.dto

import com.wadhams.financials.db.preload.type.Status
import groovy.transform.ToString

@ToString(includeNames=true)
class SuncorpDTO {
	Status csvStatus
	//csv data below
	String transactionDate
	BigDecimal amount
	String payee
	String description
	String category
}
