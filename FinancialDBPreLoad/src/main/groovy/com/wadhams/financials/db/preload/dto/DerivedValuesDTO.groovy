package com.wadhams.financials.db.preload.dto

import groovy.transform.ToString

@ToString(includeNames=true)
class DerivedValuesDTO {
	String payee
	String description
	String category
	
	boolean isValid() {
		return description != null
	}
}
