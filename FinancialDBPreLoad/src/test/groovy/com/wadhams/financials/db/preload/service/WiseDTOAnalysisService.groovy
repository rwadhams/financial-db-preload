package com.wadhams.financials.db.preload.service

import com.wadhams.financials.db.preload.dto.WiseDTO
import java.text.NumberFormat

class WiseDTOAnalysisService {
	def findNonNZTransactions(List<WiseDTO> wiseDTOList) {
		println 'NonNZTransactions...'
		wiseDTOList.each {w ->
			if (w.direction == 'OUT' && w.targetCurrency != 'NZD' && w.idPrefix == 'CARD_TRANSACTION' && w.status == 'COMPLETED') {
				println w
			}
		}
		println ''
	}
	
	def analyseWiseDTOList(List<WiseDTO> wiseDTOList) {
		println 'WiseDTO list analysis...'
		println 'Unique data values:'
		//csvStatus
		Set uniqueCSVStatusSet = []
		wiseDTOList.each {w ->
			uniqueCSVStatusSet << w.csvStatus
		}
		println "\tcsvStatus........: ${uniqueCSVStatusSet}"
		
		//idPrefix
		Set uniqueIDPrefixSet = []
		wiseDTOList.each {w ->
			uniqueIDPrefixSet << w.idPrefix
		}
		println "\tidPrefix.........: ${uniqueIDPrefixSet}"
		
		//status
		Set uniqueStatusSet = []
		wiseDTOList.each {w ->
			uniqueStatusSet << w.status
		}
		println "\tstatus...........: ${uniqueStatusSet}"
		
		//direction
		Set uniqueDirectionSet = []
		wiseDTOList.each {w ->
			uniqueDirectionSet << w.direction
		}
		println "\tdirection........: ${uniqueDirectionSet}"
		
		//targetCurrency
		Set uniqueTargetCurrencySet = []
		wiseDTOList.each {w ->
			uniqueTargetCurrencySet << w.targetCurrency
		}
		println "\ttargetCurrency...: ${uniqueTargetCurrencySet}"
		
		println ''
	}
	
	def printAll(List<WiseDTO> wiseDTOList) {
		println 'WiseDTO printAll...'
		wiseDTOList.each {w ->
			println w
		}
		println ''
	}
	
}
