package com.wadhams.financials.db.preload.controller

import com.wadhams.financials.db.preload.dto.WiseDTO
import com.wadhams.financials.db.preload.service.WiseCSVService
import com.wadhams.financials.db.preload.service.WiseDTOAnalysisService
import com.wadhams.financials.db.preload.service.WiseService

class TestController {
	def researchDataFile(String fn) {
		println 'researchDataFile...'
		println ''
		
		File csvFile = getDataFile(fn)
		WiseCSVService wiseCSVService = new WiseCSVService()
		List<WiseDTO> wiseDTOList = wiseCSVService.buildWiseDTOList(csvFile)
		println "wiseDTOList size: ${wiseDTOList.size()}"
		println ''
		
		WiseService wiseService = new WiseService()
		List<WiseDTO> combinedWiseDTOList = wiseService.combineDuplicateIds(wiseDTOList)
		println "combinedWiseDTOList size: ${combinedWiseDTOList.size()}"
		println ''

		int researchCount = 0
		println 'Card Transactions...'
		combinedWiseDTOList.each {w ->
			if (w.idPrefix == 'CARD_TRANSACTION' && w.status != 'REFUNDED') {
				println w.displayValues()
				researchCount++
			}
		}
		println ''

		println 'Refunds...'
		combinedWiseDTOList.each {w ->
			if (w.status == 'REFUNDED') {
				println w.displayValues()
				researchCount++
			}
		}
		println ''

		println 'Transfers in...'
		combinedWiseDTOList.each {w ->
			if (w.idPrefix == 'TRANSFER' && w.direction == 'IN') {
				println w.displayValues()
				researchCount++
			}
		}
		println ''

		println 'Transfers out...'
		combinedWiseDTOList.each {w ->
			if (w.idPrefix == 'TRANSFER' && w.direction == 'OUT') {
				println w.displayValues()
				researchCount++
			}
		}
		println ''

		println 'Card Order...'
		combinedWiseDTOList.each {w ->
			if (w.idPrefix == 'CARD_ORDER') {
				println w.displayValues()
				researchCount++
			}
		}
		println ''
		println "Research transactions: $researchCount"
		

//		println 'NonNZTransactions...'
//		combinedWiseDTOList.each {w ->
//			if (w.direction == 'OUT' && w.targetCurrency != 'NZD' && w.idPrefix == 'CARD_TRANSACTION' && w.status == 'COMPLETED') {
//				println w
//			}
//		}
	}
	
	def analyseDataFile(String fn) {
		println 'analyseDataFile...'
		println ''
		
		File csvFile = getDataFile(fn)
		WiseCSVService wiseCSVService = new WiseCSVService()
		List<WiseDTO> wiseDTOList = wiseCSVService.buildWiseDTOList(csvFile)
		println "wiseDTOList size: ${wiseDTOList.size()}"
		println ''
		
		WiseService wiseService = new WiseService()
		List<WiseDTO> combinedWiseDTOList = wiseService.combineDuplicateIds(wiseDTOList)
		println "combinedWiseDTOList size: ${combinedWiseDTOList.size()}"
		println ''

		WiseDTOAnalysisService wiseDTOAnalysisService = new WiseDTOAnalysisService()
		wiseDTOAnalysisService.printAll(combinedWiseDTOList)
		wiseDTOAnalysisService.analyseWiseDTOList(combinedWiseDTOList)
	}
	
	File getDataFile(String fn) {
		URL resource = getClass().getClassLoader().getResource(fn)
		if (resource == null) {
			throw new IllegalArgumentException("$fn not found!")
		}
		else {
			return new File(resource.toURI())
		}
	}
}
