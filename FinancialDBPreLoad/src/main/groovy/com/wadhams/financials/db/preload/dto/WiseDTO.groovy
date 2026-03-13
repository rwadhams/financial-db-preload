package com.wadhams.financials.db.preload.dto

import com.wadhams.financials.db.preload.type.Status

import groovy.transform.ToString

@ToString(includeNames=true)
class WiseDTO {
	Status csvStatus
	//csv data below
	String idPrefix
	String id
	String status
	String direction
	String created
	String finished
	String sourceFeeAmount
	String sourceFeeCurrency
	String targetFeeAmount
	String targetFeeCurrency
	String sourceName
	String sourceAmountAfterFees
	String sourceCurrency
	String targetName
	String targetAmountAfterFees
	String targetCurrency
	String exchangeRate
	String reference
	String batch
	String createdBy
	String category

	//default constructor
	def WiseDTO() {
		
	}
		
	//copy constructor	
	def WiseDTO(WiseDTO other) {
		this.csvStatus = other.csvStatus
		this.idPrefix = other.idPrefix
		this.id = other.id
		this.status = other.status
		this.direction = other.direction
		this.created = other.created
		this.finished = other.finished
		this.sourceFeeAmount = other.sourceFeeAmount
		this.sourceFeeCurrency = other.sourceFeeCurrency
		this.targetFeeAmount = other.targetFeeAmount
		this.targetFeeCurrency = other.targetFeeCurrency
		this.sourceName = other.sourceName
		this.sourceAmountAfterFees = other.sourceAmountAfterFees
		this.sourceCurrency = other.sourceCurrency
		this.targetName = other.targetName
		this.targetAmountAfterFees = other.targetAmountAfterFees
		this.targetCurrency = other.targetCurrency
		this.exchangeRate = other.exchangeRate
		this.reference = other.reference
		this.batch = other.batch
		this.createdBy = other.createdBy
		this.category = other.category
	}
	
	String displayValues() {
		return "csvStatus: ${csvStatus.toString().padRight(6, ' ')} idPrefix: ${idPrefix.padRight(16, ' ')} status: ${status.padRight(9, ' ')} direction: ${direction.padRight(3, ' ')} Date: $created id: ${id} ($sourceCurrency->$targetCurrency) targetAmountAfterFees: ${targetAmountAfterFees} ($sourceName->$targetName) category: ${category}"
	}

}
